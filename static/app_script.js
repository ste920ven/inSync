WEB_SOCKET_SWF_LOCATION = "/WebSocketMain.swf";
WEB_SOCKET_DEBUG = true;
PATH = '/static/uploads/';

var button;
var userInfo;
var fb_name, fb_id,type;
var logged_in;
var current_accordion = 'collapseOne';
var listedAdmins = [];
var listedUsers = [];
var urlnotready = true;

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
		}, {scope:''});  	
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
	logged_in = true;
	$('#fbbypass').hide();
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
	    logout();
	});
    };
}

function unflogin(){
    if(fb_name == null || fb_name == '')
	$('#unflogin').modal('show');
    else
	return;
}
function unflogin2(name){
    fb_name = name;
    fb_id = null;
    logged_in = true;
    $('#fbbypass').hide();
    showView('nickname');
    $('#user-stuff').html('<img class="pull-right" id="user-pic" src=""><p class="pull-right" id="user-name"></p>');
    $('#user-pic').attr('src','https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSn3HISFITZIo5Hw23rw7DpW0-UuhhXeGfkW9cgvwc9v0qHpxBwgg');
    $('#user-name').html(fb_name);
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
    button = document.getElementById('user-pic');
    button.onclick = function() {
	logout();
    };
}
function logout(){
    logged_in = false;
    $('#fbbypass').show();
    if(fb_id == null)
	$('#'+fb_id+fb_name).remove();
    fb_name = null;
    fb_id = null;
    showView('login');
    $('#user-stuff').html('');
}

// socket.io specific code
var socket = io.connect();
var room = '';

socket.on('connect', function () {
    console.log('connected');
});

socket.on('userdisconnected',function(name){
    console.log(name);
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

socket.on('playpause', function(t){
    ap = document.getElementById('audioplayer');
    if(t){
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
socket.on('endit', function(time){
    ap = document.getElementById('audioplayer');
    ap.currentTime = ap.duration;
});

function toggle(){
    ap = document.getElementById('audioplayer');
    if(ap.paused)
	socket.emit('toggle play pause', room, true);
    else
	socket.emit('toggle play pause', room, false);
}
function skipTen(){
    socket.emit('skip seven', room);
}
function resetTime(){
    socket.emit('reset', room);
}
function timeChanged(){
    ap = document.getElementById('audioplayer');
    time = ap.currentTime;
    socket.emit('time changed', room, time);
    ap.removeEventListener('seeked',arguments.callee,false);
}
function itEnded(){
    ap = document.getElementById('audioplayer');
    socket.emit('end it', room);
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
function clearUsers(room){
    $.getJSON('/clear_users',{r:room});
}
function submitRoom(){
    $('#rwarning').removeClass('warning');
    var bool = true;
    if(ap.buffered.length == 0)
	bool = false;
    socket.emit('nickname', 
		fb_name, fb_id, bool, 'user', $('#room').val(), 
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
    var bool = true;
    if(ap.buffered.length == 0)
	bool = false;
    socket.emit('nickname',
		fb_name, fb_id, bool, 'admin', $('#adroom').val(),
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
    var bool = true;
    if(ap.buffered.length == 0)
	bool = false;
    socket.emit('nickname',
		fb_name, fb_id, bool, 'admin', $('#croom').val(),
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

socket.on('choose_song', function(file){
    playmysong(file);
});
socket.on('choose_url', function(url){
    playmyurl(url);
});
function choose(file){
    urlnotready = false;
    socket.emit('chose_song', room, file);
}
function chooseURL(url){
    if(urlnotready)
	choose('notasong.mp3');
    socket.emit('chose_url',room, url);
}
function playmysong(file){
    $('#audiosource').attr('src',PATH + file);
    ap = document.getElementById('audioplayer');
    ap.load();
    $('#SelectM').modal('hide');
    ap.play();
    if(ap.buffered.length == 0)
	cantPlay();
}

function playmyurl(url){
    $('#audiosource').attr('src',url);
    ap = document.getElementById('audioplayer');
    ap.load();
    $('#SelectM').modal('hide');
    ap.play();
    if(ap.buffered.length == 0)
	cantPlay();
}
function fetch(){
    var songs;
    if($('#songs ul').html() == '')
	$('#songs ul').append('<i class="icon-spinner icon-spin"></i><p>Fetching...<p>');
    $.getJSON('/get_songs',function(data) {
	songs = data.result;
	$('#songs ul').html('');
	for(var i = 0; i < songs.length; i++){
	    $('#songs ul').append('<li><a href="#" data-value="'+ songs[i] +'">'+songs[i]+'</a></li>');
	}
	$('#fetch').html('Refresh');
	$('#songs ul li a').click(function(){
	    choose($(this).attr('data-value'));
	    $('#SelectM').modal('toggle');
	});
    });
    if($('#songs ul').html() == '<i class="icon-spinner icon-spin"></i><p>Fetching...<p>')
	$('#songs ul').append('<p>None</p>');
}

function showView(desired){
    if(desired == 'login'){
	$('#nickname').hide();
	$('#audio').hide();
	$('#fb-postlog').css('display','block');
	$('#users').hide();
    }
    else if(desired == 'nickname'){
	$('#fb-postlog').hide();
	$('#audio').hide();
	$('#nickname').css('display','block');
	$('#users').hide();
    }
    else if(desired == 'audio'){	
	$('#nickname').hide();
	$('#fb-postlog').hide();
	$('#audio').css('display','block');
	$('#audiocontrols').hide();
	$('#uploadcontrols').hide();
	$('#users').css('display','block');
	listedUsers = [];
	listedAdmins = [];
	type = 'user';
	addUserToList(fb_name,fb_id,ap.buffered.length != 0);
	if(ap.buffered.length == 0)
	    cantPlay();
	getUsers();
    }
    else{
	$('#nickname').hide();
	$('#fb-postlog').hide();
	$('#audio').css('display','block');
	$('#users').css('display','block');
	listedUsers = [];
	listedAdmins = [];
	type = 'admin';
	addAdminToList(fb_name,fb_id,ap.buffered.length != 0);
	if(ap.buffered.length == 0)
	    cantPlay();
	getUsers();	
    }
}

socket.on('gotUsers', function(data){
    for(var i = 0; i < data.length; i++){
	if(data[i][3] == 'user')
	    addUser(data[i]);
	else
	    addAdmin(data[i]);
    }
});
function getUsers(){
    socket.emit('get users',room);
}
function addUser(data){
    for(var i = 0; i < listedUsers.length; i++){
	if(listedUsers[i] == (data[1] + data[0])){
	    $('#'+data[1]+data[0]).attr('data-success',data[2]);
	    return;
	}
    }
    addUserToList(data[0],data[1],data[2]);
}
function addAdmin(data){
    for(var i = 0; i < listedAdmins.length; i++){
	if(listedAdmins[i] == (data[1] + data[0])){
	    $('#'+data[1]+data[0]).attr('data-success',data[2]);
	    return;
	}
    }
    addAdminToList(data[0],data[1],data[2]);
}
function addUserToList(name,id,success){
    listedUsers.push(id+name);
    if($('#users-users').next().children().children().html() == 'None')
	$('#users-users').next().children().remove();
    var dname = '<p>'+name+'</p>'
    var img;
    if(id == null)
	img = '<img src="https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSn3HISFITZIo5Hw23rw7DpW0-UuhhXeGfkW9cgvwc9v0qHpxBwgg">';
    else
	img = '<img src="https://graph.facebook.com/' + id + '/picture">';
    $('#users-users').next().append('<tr id="'+id+name+'" data-success="'+success+'"><td>' + img + dname +'</td></tr>')
    $('#users-users').next().children().children().css('padding','0px');
    resizeUsers();
    highlightList();
}
function addAdminToList(name,id,success){
    listedAdmins.push(id+name);
    if($('#users-admins').next().children().children().html() == 'None')
	$('#users-admins').next().children().remove();
    var dname = '<p>'+name+'</p>'
    var img;
    if(id == null)
	img = '<img src="https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcSn3HISFITZIo5Hw23rw7DpW0-UuhhXeGfkW9cgvwc9v0qHpxBwgg">';
    else
	img = '<img src="https://graph.facebook.com/' + id + '/picture">';
    $('#users-admins').next().append('<tr id="'+id+name+'" data-success="'+success+'"><td>' + img + dname +'</td></tr>')
    $('#users-admins').next().children().children().css('padding','0px');
    resizeUsers();
    highlightList();
}
function highlightList(){
    $('#users tbody tr').each(function(){
	if($(this).children().html() == 'None')
	    return;
	if($(this).attr('data-success') == 'true'){
	    $(this).addClass('success');
	}
	else
	    $(this).addClass('warning');
    });
}
function resizeUsers(){
    var h = $('#users').css('height');
    var nh = h.replace(/\D+/,'') * -.5;
    $('#users').css('margin-top',nh);
}
socket.on('update_user',function(name,id){
    if($('#'+id+name).hasClass('warning'))
	$('#'+id+name).removeClass('warning');
    $('#'+id+name).addClass('success');
});
socket.on('update_user_cant',function(name,id){
    if($('#'+id+name).hasClass('success'))
	$('#'+id+name).removeClass('success');
    $('#'+id+name).addClass('warning');
});
function canPlay(){
    console.log('canplay');
    if(fb_name != null && fb_name != '')
	socket.emit('can play', room, fb_name, fb_id,type);
}
function cantPlay(){
    console.log('cantplay');
    if(fb_name != null && fb_name != '')
	socket.emit('cant play',room, fb_name, fb_id,type);
}
$(document).ready(function(){
    resizeUsers();
    ap = document.getElementById('audioplayer');
    ap.addEventListener('seeked', timeChanged);
    ap.addEventListener('ended', itEnded);
    $('#fbbypass').click(unflogin);
    $('#unfform').keydown(function(){
	if(event.keyCode == 13){
	    unflogin2($('#unfform').val());
	    $('#unflogin').modal('hide');
	}	    
    });
    $('#unfbtn').click(function(){
	unflogin2($('#unfform').val());
	$('#unflogin').modal('hide');
    });
    $('#toggle').click(toggle);
    $('#skipTen').click(skipTen);
    $('#resetTime').click(resetTime);
    $('#room').keydown(function(){
	if(event.keyCode == 13){
	    $('#rwarning').html('<i class="icon-spinner icon-spin"></i>Locating...');
	    submit();
	}
    });
    $('#roomButton').click(function(){
	$('#rwarning').html('<i class="icon-spinner icon-spin"></i>Locating...');
	submit();
    });
    $('#room').keydown(function(){
	if($('#rwarning').html() != '')
	    $('#rwarning').html('');
    });
    $('#adroom').keydown(function(){
	if(event.keyCode == 13){
	    $('#adwarning').html('<i class="icon-spinner icon-spin"></i>Verifying...');
	    adsubmit();
	}
    });
    $('#pass').keydown(function(){
	if(event.keyCode == 13){
	    $('#adwarning').html('<i class="icon-spinner icon-spin"></i>Verifying...');
	    adsubmit();
	}
    });
    $('#adButton').click(function(){
	$('#adwarning').html('<i class="icon-spinner icon-spin"></i>Verifying...');
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
	    $('#cwarning').html('<i class="icon-spinner icon-spin"></i>Creating...');
	    csubmit();
	}
    });
    $('#cButton').click(function(){
	$('#cwarning').html('<i class="icon-spinner icon-spin"></i>Creating...');
	csubmit();
    });
    $('#fetch').click(fetch);
    $('#url').keydown(function(){
	if(event.keyCode == 13)
	    chooseURL($('#url').val());
    });
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
    
    (function(){
	var bar=$('.bar');
	var percent=$('.percent');
	var status=$('#status');
	$('#myForm').ajaxForm({
	    beforeSubmit:function(){
		if($('input[name=file]').val() == ''){
		    status.html('Please select a file');
		    return false;
		}
	    },
	    beforeSend:function(){
		status.empty();
		var percentVal='0%';
		bar.width(percentVal)
		percent.html(percentVal);
	    }, 
	    uploadProgress:function(event,position,total,percentComplete){
		var percentVal=percentComplete+'%';
		bar.width(percentVal)
		percent.html(percentVal);
	    }, 
	    success:function(){
		var percentVal='100%';
		bar.width(percentVal)
		percent.html(percentVal);
	    }, 
	    complete:function(xhr){
		var result = $.parseJSON( xhr.responseText);
		if(result.result == "File upload failed: unknown reason")
		    $('#progress').addClass('progress-danger');
		else
		    $('#progress').removeClass('progress-danger');
		status.html(result.result);
	    }});
    })();
    $('#tool').tooltip();
});
