import os
import werkzeug.serving
from werkzeug import secure_filename
from flask import Flask, request, render_template, redirect, url_for, send_from_directory
from gevent import monkey; monkey.patch_all()
from socketio import socketio_manage
from socketio.namespace import BaseNamespace
from socketio.mixins import RoomsMixin, BroadcastMixin
from socketio.server import SocketIOServer
from cmixin import CustomMixin

app = Flask(__name__)
global _name

UPLOAD_FOLDER = './static/uploads'
ALLOWED_EXTENSIONS = set(['mp3'])
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# The socket.io namespace
class ChatNamespace(BaseNamespace, RoomsMixin, BroadcastMixin, CustomMixin):
    def on_nickname(self, nickname, room):
        self.environ.setdefault('nicknames', []).append(nickname)
        self.socket.session['nickname'] = nickname
        self.broadcast_event('nicknames', self.environ['nicknames'])
        self.join(room)
    """
    def on_user_message(self, msg):
        self.emit_to_room('main_room', 'msg_to_room', self.socket.session['nickname'], msg.upper())
        """
    def on_toggle_play_pause(self, room):
        print room
        self.emit_to_room_and_you(room, 'playpause')

    def on_skip_seven(self, room):
        self.emit_to_room_and_you(room, 'skip')

    def on_reset(self, room):
        self.emit_to_room_and_you(room, 'reset')

    def on_time_changed(self, room, time):
        self.emit_to_room(room, 'updateTime', time)
        
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

def list_files():
    folder = './static/uploads/'
    print os.listdir(folder)
    
@app.route("/", methods = ["GET", "POST"])
def home():
    if request.method == "GET":
        return render_template("index.html")

@app.route("/others.html", methods = ["GET", "POST"])
def others():
    if request.method == "GET":
        return render_template("others.html")

@app.route("/app", methods = ['GET', 'POST'])
def index():
    if request.method == "GET":
        return render_template('app_index.html')

@app.route('/upload.html', methods = ['GET','POST'])
def upload():
    if request.method == 'POST':
        print 'post'
        file = request.files['file']
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
            print 'successfully uploaded'
            return render_template('upload.html')
    if request.method == 'GET':
        return render_template('upload.html')
        
@app.route("/socket.io/<path:path>")
def run_socketio(path):
    socketio_manage(request.environ, {'': ChatNamespace})

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'],
                               filename)

@werkzeug.serving.run_with_reloader
def run_dev_server():
    print " * Running on http://127.0.0.1:5000/"
    app.debug = True
    port = 5000
    SocketIOServer(('', port), app, resource="socket.io").serve_forever()

if __name__ == "__main__":
    run_dev_server()
