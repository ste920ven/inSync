WEB_SOCKET_SWF_LOCATION = "/WebSocketMain.swf";
WEB_SOCKET_DEBUG = true;
PATH = '/static/uploads/';

var button;
var userInfo;
var fb_name, fb_id;
var logged_in;
var current_accordion = 'collapseOne';

function showView(desired){
    if(desired == 'login'){
	$('#nickname').hide();
	$('#audio').hide();
	$('#fb-postlog').css('display','block');
    }
    else if(desired == 'nickname'){
	$('#fb-postlog').hide();
	$('#audio').hide();
	$('#nickname').css('display','block');
    }
    else if(desired == 'audio'){	
	$('#nickname').hide();
	$('#fb-postlog').hide();
	$('#audio').css('display','block');
	$('#audiocontrols').hide();
    }
    else{
	$('#nickname').hide();
	$('#fb-postlog').hide();
	$('#audio').css('display','block');
    }
}

//fb stuff
window.fbAsyncInit = function() {
    FB.init({ appId: '378043245648833', 
	      status: true, 
	      cookie: true,
	      xfbml: true,
	      oauth: true});
    
    function updateButton(response) {
	button = document.getElementById('fb-postlog');
	userInfo = document.getElementById('user-info');

	if (response.authResponse) {
	    //user is already logged in and connected
	    FB.api('/me', function(info) {
		login(response, info);
	    });
	} else {
	    //user is not connected to your app or logged out
	    button.onclick = function() {
		FB.login(function(response) {
		    if (response.authResponse) {
			FB.api('/me', function(info) {
			    login(response, info);
			});	   
		    } else {
			//user cancelled login or did not grant authoriz.
		    }
		}, {scope:'email,user_about_me'});  	
	    }
	}
    }
    
    // run once with current status and whenever the status changes
    FB.getLoginStatus(updateButton);
    FB.Event.subscribe('auth.statusChange', updateButton);	
};
$(document).ready(function() {
    (function() {
	var e = document.createElement('script'); e.async = true;
	e.src = 'http://connect.facebook.net/en_US/all.js';
	document.getElementById('fb-root').appendChild(e);
    }());
});

function login(response, info){
    fb_name = info.name;
    fb_id = info.id;
    if (response.authResponse) {
	var accessToken = response.authResponse.accessToken;
	showView('nickname');
	$('#user-stuff').html('<img class="pull-right" id="user-pic" src=""><p class="pull-right" id="user-name"></p>');
	$('#user-pic').attr('src','https://graph.facebook.com/' + info.id + '/picture');
	$('#user-name').html(info.name);
	$('#user-pic').hover(
	    function() {
		var $this = $('#user-name'); // caching $(this)
		$this.data('initialText', $this.text());
		$this.css('color','white');
		$this.text("Click to logout");
	    },
	    function() {
		var $this = $('#user-name'); // caching $(this)
		$this.css('color','gray');
		$this.text($this.data('initialText'));
	    });
    }
    button = document.getElementById('user-pic');
    button.onclick = function() {
	FB.logout(function(response) {
	    logout(response);
	});
    };
}

function logout(response){
    showView('login');
    $('#user-stuff').html('');
}

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
    console.log('cool');
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
    $.getJSON('/room_exists',{
	r: $('#room').val()
    }, function(data){
	if(data.result == 'DNE'){
	    $('#rwarning').addClass('warning');
	    $('#rwarning').html('Room does not exist');
	    return false;
	}
	else
	    submitRoom();
    });
}
function adsubmit(){
    $.getJSON('/room_exists',{
	r: $('#adroom').val(),
	p: $('#pass').val()
    }, function(data){
	console.log(data.result);
	if(data.result == 'DNE'){
	    $('#adwarning').addClass('warning');
	    $('#adwarning').html('Room does not exist');
	    return false;
	}
	else if(data.result == 'INVALID'){
	    $('#adwarning').addClass('warning');
	    $('#adwarning').html('Invalid password');
	    return false;
	}
	else{
	    adsubmitRoom();
	}	   
    });
}
function csubmit(){
    if($('#croom').val() == ''){
	$('#cwarning').addClass('warning');
	$('#cwarning').html('Enter a room name');
	return false;
    }
    $.getJSON('/create_room',{
	r: $('#croom').val(),
	p: $('#cpass').val()
    }, function(data){
	console.log(data.result);
	if(data.result == 'TAKEN'){
	    $('#cwarning').addClass('warning');
	    $('#cwarning').html('Room name taken');
	    return false;
	}
	else{
	    csubmitRoom();
	}
    });
}
function submitRoom(){
    $('#rwarning').removeClass('warning');
    socket.emit('nickname', 
		fb_name, $('#room').val(), 
		function (set) {
		    if (!set) {
			room = $('#room').val();
			$('#userID').html('User: ' + fb_name);
			$('#roomID').html('Room: ' + room);
			showView('audio');
		    }
		});
    return false;
}
function adsubmitRoom(){
    $('#adwarning').removeClass('warning');
    socket.emit('nickname',
		fb_name, $('#adroom').val(),
		function (set) {
		    if(!set) {
			room = $('#adroom').val();
			$('#userID').html('User: ' + fb_name);
			$('#roomID').html('Room: ' + room);
			showView('adaudio');
		    }
		});
    return false;
}
function csubmitRoom(){
    $('#cwarning').removeClass('warning');
    socket.emit('nickname',
		fb_name, $('#croom').val(),
		function (set) {
		    if(!set) {
			room = $('#croom').val();
			$('#userID').html('User: ' + fb_name);
			$('#roomID').html('Room: ' + room);
			showView('adaudio');
		    }
		});
    return false;
}
function playmysong(){
    $('#audiosource').attr('src',PATH + 'mysong.mp3');
    ap = document.getElementById('audioplayer');
    ap.load();
}

$(document).ready(function(){
    $('#room').keydown(function(){
	if(event.keyCode == 13){
	    $('#rwarning').html('Locating...');
	    submit();
	}
    });
    $('#roomButton').click(function(){
	$('#rwarning').html('Locating...');
	submit();
    });
    $('#room').keydown(function(){
	if($('#rwarning').html() != '')
	    $('#rwarning').html('');
    });
    $('#pass').keydown(function(){
	if(event.keyCode == 13){
	    $('#adwarning').html('Verifying...');
	    adsubmit();
	}
    });
    $('#adButton').click(function(){
	$('#adwarning').html('Verifying...');
	adsubmit();
    });
    $('#adroom').keydown(function(){
	if($('#adwarning').html() != '')
	    $('#adwarning').html('');
    });
    $('#pass').keydown(function(){
	if($('#adwarning').html() != '')
	    $('#adwarning').html('');
    });
    $('#croom').keydown(function(){
	if($('#cwarning').html() != '')
	    $('#cwarning').html('');
    });
    $('#cpass').keydown(function(){
	if($('#cwarning').html() != '')
	    $('#cwarning').html('');
    });
    $('#cpass').keydown(function(){
	if(event.keyCode == 13){
	    $('#cwarning').html('Creating');
	    csubmit();
	}
    });
    $('#cButton').click(function(){
	$('#cwarning').html('Creating');
	csubmit();
    });
    
			
    ap = document.getElementById('audioplayer');
    ap.addEventListener('seeked', timeChanged);
    $('.accordion-toggle').hover(
	function(){
	    $(this).parent().css('background-color','#0088cc');
	}, function(){
	    $(this).parent().css('background-color','black');
	});
    
    $('.accordion-heading').click(function(){
	if($(this).next().attr('id') == current_accordion){
	    $(this).next().collapse('hide');
	    $(this).children('i').attr('class','icon-circle-arrow-down icon-white pull-left');
	    current_accordion = null;
	}
	else if(current_accordion == null){
	    $(this).next().collapse('show');
	    $(this).children('i').attr('class','icon-circle-arrow-up icon-white pull-left');
	    current_accordion = $(this).next().attr('id');
	}
	else{
	    var s = '#' + current_accordion;
	    $(s).collapse('hide');
	    $(s).prev().children('i').attr('class','icon-circle-arrow-down icon-white pull-left');
	    $(this).next().collapse('show');
	    $(this).children('i').attr('class','icon-circle-arrow-up icon-white pull-left');
	    current_accordion = $(this).next().attr('id');
	}
    });
    $('#toggle').click(toggle);
    $('#skipTen').click(skipTen);
    $('#resetTime').click(resetTime);
});
