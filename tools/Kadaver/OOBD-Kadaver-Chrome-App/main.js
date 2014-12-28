var childWindow;

chrome.app.runtime.onLaunched.addListener(function() {
    childWindow=chrome.app.window.create('index.html', {
        'bounds': {
          'width': 300,
          'height': 250
        }
    }, function() {
        //window.app = chrome.app.window.getAll()[0].contentWindow.document; 
    
   
   
});
});
chrome.runtime.onSuspend.addListener(function() {
	//Close bluetooth    
});

chrome.app.window.get(childWindow).onClosed.addListener(function(){
	chrome.bluetoothSocket.disconnect(window.socketId);
	window.websocket.close();
});
