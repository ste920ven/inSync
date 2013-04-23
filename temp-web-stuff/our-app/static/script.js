WEB_SOCKET_SWF_LOCATION = "/WebSocketMain.swf";
WEB_SOCKET_DEBUG = true;



// socket.io specific code
var socket = io.connect();

socket.on('playpause', function(){
    ap = document.getElementById('audioplayer');
    if(ap.paused){
	ap.play();
    }   
    else
	ap.pause();
});

socket.on('skip', function(){
    ap = document.getElementById('audioplayer');
    ap.currentTime += 10;
});

function toggle(){
    socket.emit('toggle play pause');
}

function skipTen(){
    socket.emit('skip seven');
}