from flask import Flask, render_template, Response, request

import werkzeug.serving
from gevent import monkey
from socketio import socketio_manage
from socketio.namespace import BaseNamespace
from socketio.server import SocketIOServer

app = Flask(__name__)
monkey.patch_all()

@app.route("/")
def hello():
    return render_template("main.html")

class ShoutsNamespace(BaseNamespace):
    sockets = {}
    def recv_connect(self):
        print "Got a socket connection" # debug
        self.sockets[id(self)] = self
    def disconnect(self, *args, **kwargs):
        print "Got a socket disconnection" # debug
        if id(self) in self.sockets:
            del self.sockets[id(self)]
        super(ShoutsNamespace, self).disconnect(*args, **kwargs)
    # broadcast to all sockets on this channel!
    @classmethod
    def broadcast(self, event, message):
        for ws in self.sockets.values():
            ws.emit(event, message)


@app.route('/socket.io/<path:rest>')
def push_stream(rest):
    try:
        socketio_manage(request.environ, {'/shouts': ShoutsNamespace}, request)
    except:
        app.logger.error("Exception while handling socketio connection",
                         exc_info=True)
    return Response()

@app.route("/shout", methods=["GET"])
def say():
    message = request.args.get('msg', None)
    if message:
        ShoutsNamespace.broadcast('message', message)
        return Response("Message shouted!")
    else:
        return Response("Please specify your message in the 'msg' parameter")
    
@werkzeug.serving.run_with_reloader
def run_dev_server():
    app.debug = True
    port = 6020
    SocketIOServer(('', port), app, resource="socket.io").serve_forever()

if __name__ == "__main__":
    run_dev_server()
