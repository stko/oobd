chrome.app.runtime.onLaunched.addListener(function() {
    chrome.app.window.create('index.html', {
        'bounds': {
          'width': 475,
          'height': 500
        }
    }, function() {
        //window.app = chrome.app.window.getAll()[0].contentWindow.document; 
    
   
   
});
});
chrome.runtime.onSuspend.addListener(function() {
    //Close bluetooth    
});

