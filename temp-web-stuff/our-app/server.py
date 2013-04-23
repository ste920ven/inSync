import os
import werkzeug.serving
from gevent import monkey; monkey.patch_all()
from flask import Flask, request, render_template
from socketio import socketio_manage
from socketio.namespace import BaseNamespace
from socketio.mixins import RoomsMixin, BroadcastMixin
from socketio.server import SocketIOServer

app = Flask(__name__)

# The socket.io namespace
class ChatNamespace(BaseNamespace, RoomsMixin, BroadcastMixin):
    """
    def on_nickname(self, nickname):
        self.environ.setdefault('nicknames', []).append(nickname)
        self.socket.session['nickname'] = nickname
        self.broadcast_event('announcement', '%s has connected' % nickname)
        self.broadcast_event('nicknames', self.environ['nicknames'])
        # Just have them join a default-named room
        self.join('main_room')

    def on_user_message(self, msg):
        self.emit_to_room('main_room', 'msg_to_room', self.socket.session['nickname'], msg.upper())
        """
    def on_toggle_play_pause(self):
        self.broadcast_event('playpause')

    def on_skip_seven(self):
        self.broadcast_event('skip')
        
# Flask routes
@app.route('/')
def index():
    return render_template('chat.html')

@app.route("/socket.io/<path:path>")
def run_socketio(path):
    socketio_manage(request.environ, {'': ChatNamespace})
    
@werkzeug.serving.run_with_reloader
def run_dev_server():
    app.debug = True
    port = 8000
    SocketIOServer(('', port), app, resource="socket.io").serve_forever()

if __name__ == "__main__":
    run_dev_server()
