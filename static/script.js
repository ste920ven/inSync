$(document).ready(function() {
    $('#middle').css('display','none');
    $('#middle').fadeIn(1000);
    $('a.transitit').click(function(event){
	event.preventDefault();
	sessionStorage.target = this.innerHTML;
	linkLocation = this.href;
	$('#middle').fadeOut(400, redirectPage);
    });
});

function redirectPage(){
    window.location = linkLocation;
}
			 