WEB_SOCKET_SWF_LOCATION = "/WebSocketMain.swf";
WEB_SOCKET_DEBUG = true;

// socket.io specific code
var socket = io.connect();

//for yahoo WP
socket.on('playpause', function(){
    ap = document.getElementById('audioplayer');
    if(ap.paused){
	ap.play();
    }   
    else
	ap.pause();
});

function toggle(){
    socket.emit('toggle play pause');
}