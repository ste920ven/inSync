WEB_SOCKET_SWF_LOCATION = "/WebSocketMain.swf";
WEB_SOCKET_DEBUG = true;

// socket.io specific code
var socket = io.connect();
var room = '';

socket.on('connect', function () {
    console.log('connected');
});

socket.on('reconnect', function () {
    $('#lines').remove();
    console.log('Reconnected to the server');
});

socket.on('reconnecting', function () {
    console.log('Attempting to re-connect to the server');
});

socket.on('error', function (e) {
    console.log(e ? e : 'An unknown error occured');
});

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

socket.on('reset', function(){
    ap = document.getElementById('audioplayer');
    ap.currentTime = 0;
});

function toggle(){
    socket.emit('toggle play pause', room);
}

function skipTen(){
    socket.emit('skip seven');
}
function resetTime(){
    socket.emit('reset');
}

$(function () {
    $('#set-nickname').submit(function (ev) {
        socket.emit('nickname', 
		    $('#nick').val(), $('#room').val(), 
		    function (set) {
			if (!set) {
			    room = $('#room').val();
			    $('#audio').css('display','inline');
			    $('#userID').html('User: ' + $('#nick').val());
			    $('#roomID').html('Room: ' + room);
			    return $('#set-nickname').hide();
			}
			//$('#nickname-err').css('visibility', 'visible');
		    });
        return false;
    });
});