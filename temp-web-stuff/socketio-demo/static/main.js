addMessage = function(text) {
    var div = $("<div>").addClass("msg").text(text);
    $("#target").prepend(div);
}


$(document).ready(function() {
    var socket = io.connect('/shouts');
    socket.on('connect', function() {
        console.log("socket connected");
    });
    socket.on('disconnect', function() {
        console.log("socket disconnected");
    });

    socket.on('message', function(data) {
        console.log("Got message:", data);
        addMessage(data);
    });
});