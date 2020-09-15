var stompClient = null;
var serverAddress = null;

function setConnected(connected,username) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
        $("#connectedUser").append("<label> user: "+username+"</label>");
    }
    else {
    	$("#connectedUser").html("");
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
	var socket = null;
	if(serverAddress !== null && (serverAddress.includes(".") || serverAddress.includes(":")))		
		socket = new SockJS('https://'+serverAddress+'/extract-image');
	else
		socket = new SockJS('/extract-image');
	
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true,frame.headers["user-name"]);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/output', function (greeting) {
            showGreeting(JSON.parse(greeting.body));
        });
        stompClient.subscribe('/output', function (greeting) {
           console.log(JSON.parse(greeting.body).job_id);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
	serverAddress = $("#name").val(); //Set server address	
}

function showGreeting(message) {
	if(message.pageContent !== undefined && message.pageContent !== ""){
		$("#greetings").append("<tr><td style='font-weight: bold;'>Folder: "+message.documentPath+" File: "+message.pageFileName+"</td></tr>");
		$("#greetings").append("<tr><td><img src=data:image/png;base64," + message.pageContent + " width = '20%'></td></tr>");
	}
	if(message.failureReason !== undefined && message.failureReason !== ""){
		$("#greetings").append("<tr><td style='font-weight: bold;'>"+message.documentName+"</td></tr>");
		$("#greetings").append("<tr><td style='color: red;'>"+ message.failureReason + "</td></tr>");
	}
	else if(message.pageContent === undefined){
		$("#greetings").append("<tr><td style='font-weight: bold;'>"+message+"</td></tr>");
	}
	
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});