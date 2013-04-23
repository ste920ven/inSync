from gevent import monkey; monkey.patch_all()
from flask import Flask, request, send_file, render_template
import os
from werkzeug.wsgi import SharedDataMiddleware
from socketio import socketio_manage
from socketio.namespace import BaseNamespace
from socketio.mixins import RoomsMixin, BroadcastMixin
from socketio.server import SocketIOServer

import werkzeug.serving

# The socket.io namespace
class ChatNamespace(BaseNamespace, RoomsMixin, BroadcastMixin):
    def on_nickname(self, nickname, room):
        self.environ.setdefault('nicknames', []).append(nickname)
        self.socket.session['nickname'] = nickname
        self.broadcast_event('announcement', '%s has connected' % nickname)
        self.broadcast_event('nicknames', self.environ['nicknames'])
        
        self.join(room)

    def on_user_message(self, msg, room):
        self.emit_to_room(room, 'msg_to_room', self.socket.session['nickname'], msg)

    def recv_message(self, message):
        print "PING!!!", message


# Flask routes
app = Flask(__name__)
@app.route('/')
def index():
    return render_template('chat.html')

@app.route("/socket.io/<path:path>")
def run_socketio(path):
    socketio_manage(request.environ, {'': ChatNamespace})
    
@werkzeug.serving.run_with_reloader
def run_dev_server():
    app.debug = True
    port = 6020
    SocketIOServer(('', port), app, resource="socket.io").serve_forever()

if __name__ == "__main__":
    run_dev_server()
