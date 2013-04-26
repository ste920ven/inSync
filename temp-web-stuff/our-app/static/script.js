WEB_SOCKET_SWF_LOCATION = "/WebSocketMain.swf";
WEB_SOCKET_DEBUG = true;
PATH = '/static/uploads/';

// socket.io specific code
var socket = io.connect();
var room = '';

socket.on('connect', function () {
    console.log('connected');
});

socket.on('reconnect', function () {
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

socket.on('updateTime', function(time){
    ap = document.getElementById('audioplayer');
    ap.currentTime = time;
    setTimeout(function() {
	ap.addEventListener('seeked', timeChanged);
    }, 10);
});

function toggle(){
    socket.emit('toggle play pause', room);
}
function skipTen(){
    socket.emit('skip seven', room);
}
function resetTime(){
    socket.emit('reset', room);
}
function timeChanged(){
    console.log('time');
    ap = document.getElementById('audioplayer');
    time = ap.currentTime;
    socket.emit('time changed', room, time);
    ap.removeEventListener('seeked',arguments.callee,false);
}

function submit(){
    socket.emit('nickname', 
		$('#nick').val(), $('#room').val(), 
		function (set) {
		    if (!set) {
			room = $('#room').val();
			$('#audio').css('display','inline');
			$('#userID').html('User: ' + $('#nick').val());
			$('#roomID').html('Room: ' + room);
			return $('#nickname').hide();
		    }
		    //$('#nickname-err').css('visibility', 'visible');
		});
    return false;
}

function playmysong(){
    $('#audiosource').attr('src',PATH + 'mysong.mp3');
    ap = document.getElementById('audioplayer');
    ap.load();
}

$(function(){
    $('#room').keydown(function(){
	if(event.keyCode == 13)
	    submit();
    });
});

$(document).ready(function(){
    ap = document.getElementById('audioplayer');
    ap.addEventListener('seeked', timeChanged);
});
