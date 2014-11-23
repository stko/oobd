//
//VIA https://developer.chrome.com/apps/app_bluetooth
app = window.document;
chrome.bluetooth.getAdapterState(function(adapter) {
    console.log("Adapter " + adapter.address + ": " + adapter.name);
});

var powered = false;
chrome.bluetooth.getAdapterState(function(adapter) {
    powered = adapter.powered;
});

chrome.bluetooth.onAdapterStateChanged.addListener(
    function(adapter) {
        console.log(adapter);
        if (adapter.powered != powered) {
            powered = adapter.powered;
            if (powered) {
                console.log("Adapter radio is on");
            } else {
                console.log("Adapter radio is off");
            }
        }
    });
device_names = {};
var updateDeviceName = function(device) {
    console.log("+", device);
    device_names[device.address] = device.name;
    try {
        app.getElementById("devicenames").innerHTML = "";
    } catch (e) {
        //Try again soon
        setTimeout(function() {
            updateDeviceName(device);
        }, 1000);
        return;
    }
    for (var i in device_names) {
        app.getElementById("devicenames").innerHTML += "<div class='deviceitem' data-address='" + i + "'>-" + device_names[i] + " [" + i + "]</div>";
    }
    var devicelist = app.getElementsByClassName("deviceitem");
    for (var i in devicelist) {
        if (i != parseInt(i))
            continue;
        console.log(i);
        devicelist[i].addEventListener('click', function() {
            //                        console.log("Devicelist "+i+" "+this.dataset.address);
            connectTo(this.dataset.address);
        });
    }
};
var removeDeviceName = function(device) {
    console.log("-", device);
    delete device_names[device.address];
}

// Add listeners to receive newly found devices and updates
// to the previously known devices.
chrome.bluetooth.onDeviceAdded.addListener(updateDeviceName);
chrome.bluetooth.onDeviceChanged.addListener(updateDeviceName);
chrome.bluetooth.onDeviceRemoved.addListener(removeDeviceName);

// With the listeners in place, get the list of devices found in
// previous discovery sessions, or any currently active ones,
// along with paired devices.
chrome.bluetooth.getDevices(function(devices) {
    for (var i = 0; i < devices.length; i++) {
        updateDeviceName(devices[i]);
    }
});

// Now begin the discovery process.
function discovery() {
    console.log("Starting device discovery");
    chrome.bluetooth.startDiscovery(function() {
        // Stop discovery after 30 seconds.
        console.log("discover button");
        try {
            app.getElementById("discovery").innerHTML = "Discovering";
            app.getElementById("discovery").disabled = true;
        } catch (E) {
            setTimeout(function() {
                app.getElementById("discovery").innerHTML = "Discovering";
                app.getElementById("discovery").disabled = true;
            }, 1000);
        }
        setTimeout(function() {
            app.getElementById("discovery").innerHTML = "Discover Devices";
            app.getElementById("discovery").disabled = false;
            app.getElementById("discovery").onclick = function() {
                discovery();
            };
            chrome.bluetooth.stopDiscovery(function() {
                console.log("Discovery Ends")
            });
        }, 30000);
    });
}
discovery();


var onConnectedCallback = function() {
    //            console.log("The connectTo was called back", chrome.runtime.lastError.message);
    if (chrome.runtime.lastError) {
        console.log(chrome.runtime.lastError);
        console.log("Connection failed: " + chrome.runtime.lastError.message);
        app.getElementById("bluetoothconnect").innerHTML = "Could not connect. " + chrome.runtime.lastError.message + ".";
    } else {
        // Profile implementation here.
        console.log("Profile Implementation");
        //            app.getElementById('bluetoothconnect').innerHTML = "Successfully connected to "+device_names[address];
        app.getElementById('bluetoothconnect').innerHTML = "Successfully connected => No Address In Message";
        app.getElementById('service').disabled = false;
        app.getElementById('devicenames').setAttribute('class', 'disabled');
        app.getElementById('service').addEventListener('click', function() {
            console.log("click");
            startService();
        });
        app.getElementById('disconnect').addEventListener('click', function() {
            chrome.bluetoothSocket.disconnect(window.socketId);
            app.getElementById('bluetoothstatus').innerHTML = "Nothing doing";
            app.getElementById('bluetoothconnect').innerHTML = "Not connected";
            app.getElementById('service').disabled = false;
            app.getElementById('disconnect').disabled = true;
        });
    }
};


//"49aca6e6-0596-11e4-8f46-b2227cce2b54", "49aca9f2-0596-11e4-8f46-b2227cce2b54", 
function connectTo(address) {
    app.getElementById("bluetoothconnect").innerHTML = "Trying to establish connection to " + device_names[address];
    console.log("connectTo " + device_names[address]);
    var uuid = 1105;
    var uuid = "fa87c0d0-afac-11de-8a39-0800200c9a66";
    var uuid = "00001101-0000-1000-8000-00805f9b34fb";
    var uuid = "0x1101";
    var uuid = "cc8894db-f550-4a33-8a02-fd8a99fe38fb"
    var uuid = "00001101-0000-1000-8000-00805f9b34fb";
    var uuid = "1101";
 

    chrome.bluetoothSocket.create(function(createInfo) {
        window.socketId = createInfo.socketId;
        console.log(uuid, createInfo, address);
        chrome.bluetoothSocket.connect(createInfo.socketId,
            address, uuid, onConnectedCallback);
    });
}

function startService() {
    console.log("Starting...");
    app.getElementById('bluetoothstatus').innerHTML = "Starting Connection";
    app.getElementById('disconnect').disabled = false;
    sendMessage(str2ab("\r"));

    setTimeout(function() {

        sendMessage(str2ab("p 0 0 0\r"));
    }, 200);


}

function sendMessage(data) {
    chrome.bluetoothSocket.send(window.socketId, data, function(bytes_sent) {
        if (chrome.runtime.lastError) {
            console.log("Send failed: " + chrome.runtime.lastError.message);
            app.getElementById("bluetoothstatus").innerHTML = chrome.runtime.lastError.message;
        } else {
            console.log("Sent " + bytes_sent + " bytes");
            console.log("Sent " + ab2str(data));
            app.getElementById("bluetoothstatus").innerHTML = "Sent " + bytes_sent + " bytes";
        }
    });
}

chrome.bluetoothSocket.onReceive.addListener(function(receiveInfo) {
    if (receiveInfo.socketId != socketId) {
        app.getElementById("bluetoothstatus").innerHTML = "Got data from wrong socket";
        return;
    }
    // receiveInfo.data is an ArrayBuffer.
    app.getElementById("bluetoothstatus").innerHTML = "Received " + ab2str(receiveInfo.data);
    console.log("Received: ", ab2str(receiveInfo.data));
	doSend(ab2str(receiveInfo.data));
});
chrome.bluetoothSocket.onReceiveError.addListener(function(errorInfo) {
    // Cause is in errorInfo.error.
    console.log(errorInfo.errorMessage);
    app.getElementById("bluetoothstatus").innerHTML = errorInfo.errorMessage;
});

function ab2str(buf) {
    return String.fromCharCode.apply(null, new Uint8Array(buf));
}

function str2ab(str) {
        var buf = new ArrayBuffer(str.length); // 2 bytes for each char
        var bufView = new Uint8Array(buf);
        for (var i = 0, strLen = str.length; i < strLen; i++) {
            bufView[i] = str.charCodeAt(i);
        }
        return buf;
    }


    console.log("Starting WS Work");
var wsUri = "ws://oobd.luxen.de:9000/";
var outputWS;
var thisChannel;

function init_WS() {
// create random channel number
var min = 10000000;
var max = 99999999;
thisChannel=Math.floor(Math.random()*(max-min+1)+min)+"";
console.log("Mychannel: "+thisChannel);
app.getElementById("bluetoothstatus").innerHTML =thisChannel;
app.getElementById("channel").innerHTML =thisChannel;



    outputWS = document.getElementById("outputWS");

	
// http://demo.agektmr.com/dialog/ 
	
var wsdialog = app.getElementById('wsdialog');
app.getElementById('show').onclick = function() {
  wsdialog.showModal();
};

app.getElementById('close').onclick = function() {
  var value = app.getElementById('return_value').value;
  wsdialog.close(value);
};
app.getElementById('wsdialog').addEventListener('close', function() {
  alert(this.returnValue);
});



    testWebSocket();
}

function testWebSocket() {
    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {
        onOpen(evt)
    };
    websocket.onclose = function(evt) {
        onClose(evt)
    };
    websocket.onmessage = function(evt) {
        onMessage(evt)
    };
    websocket.onerror = function(evt) {
        onError(evt)
    };
}

function showStatus(event){
	writeToScreen(event);
}


function onOpen(evt) {
    showStatus("WSCONNECTED");
    doSend("WebSocket rocks");
}

function onClose(evt) {
    showStatus("WSDISCONNECTED");
}

function onMessage(evt) {
    showStatus("WSRECEIVE");
	try {
	obj = JSON.parse(evt.data);
	if (obj.msg){
		sendMessage(str2ab(atob(obj.msg)));
	}
	}
	catch(err){
		console.log("Json Error "+err.message);
	}
// MUSS NOCH AN DIE RICHTIGE STELLE!  websocket.close();
}

function onError(evt) {
    showStatus('WSERROR');
    console.log('WS ERROR: ' + evt.data);
}

function doSend(message) {
	var msg=JSON.stringify({reply: btoa(message), channel:btoa(thisChannel)});
    showStatus('WSSEND');
    websocket.send(msg);
}

function writeToScreen(message) {
    var pre = document.createElement("p");
    pre.style.wordWrap = "break-word";
    pre.innerHTML = message;
    outputWS.appendChild(pre);
}
window.addEventListener("load", init_WS, false);


