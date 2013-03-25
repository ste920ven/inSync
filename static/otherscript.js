$(document).ready(function() {
    $('#body-container').css('display','none');
    $('#body-container').fadeIn(600);
    initTab();
    $('.brand').click(function(event){
	event.preventDefault();
	linkLocation = this.href;
	$('#body-container').fadeOut(600, redirectPage);
    });
    $('.nav a:first').click(function(event){
	event.preventDefault();
	$('#body-nav a:first').tab('show');
    });
    $('.nav a:eq(1)').click(function(event){
	event.preventDefault();
	$('#body-nav a:eq(1)').tab('show');
    });
    $('.nav a:eq(2)').click(function(event){
	event.preventDefault();
	$('#body-nav a:eq(2)').tab('show');
    });
    $('.nav a:eq(3)').click(function(event){
	event.preventDefault();
	$('#body-nav a:eq(3)').tab('show');
    });
    $('#body-container i:last').mouseenter(function(){
	$(this).attr('class','icon-remove pull-right icon-white');
    }).mouseleave(function(){
	$(this).attr('class','icon-remove pull-right');
    });
    $('#body-container i:last').click(function(){
	window.location = "/";	
    });
});

function redirectPage(){
    window.location = linkLocation;
}

function initTab(){
    console.log("it initied");
    if(sessionStorage.target == "Jobs")
	$('#body-nav a:eq(1)').tab('show');
    else if(sessionStorage.target == "Contact")
	$('#body-nav a:eq(2)').tab('show');
    else if(sessionStorage.target == "Report a Bug")
	$('#body-nav a:eq(3)').tab('show');
    else
	$('#body-nav a:first').tab('show');
}