$(document).ready(function() {
    $('#body-container').css('display','none');
    $('#body-container').fadeIn(600);
    $('#body-nav a:first').tab('show');
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