

(function() {
    // Load the script
    var script = document.createElement("SCRIPT");
    script.src = 'https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js';
    script.type = 'text/javascript';
    document.getElementsByTagName("head")[0].appendChild(script);
	console.log('irgendwo');
    // Poll for jQuery to come into existance
    var checkReady = function(callback) {
        if (window.jQuery) {
	console.log('gefunden');
            callback(jQuery);
        }
        else {
	console.log('warten');
            window.setTimeout(function() { checkReady(callback); }, 100);
        }
    };

    // Start polling...
    checkReady(function($) {
 	console.log('wo bin ich ');
       // Use $ here...
    });
})();


var readyStateCheckInterval = setInterval(function() {
    if (document.readyState === "complete") {
        clearInterval(readyStateCheckInterval);
	Oobd.init('ws://localhost:8443');
	registerOOBD("oobd_te-interface_serial:","");
	registerOOBD("oobd_te-interface_version:","");
	registerOOBD("oobd_te-interface_voltage:","");
	registerOOBD("oobd_te-interface_bus:","");

	// Get all svg images, which are defined as OOBDMap class
	var a = document.getElementsByClassName("OOBDMap");
	var oobdpatt = new RegExp("^oobd_"); // Regex pattern for svg area identification
	for (index = 0; index < a.length; ++index) { // go through all images found
		var svgDoc = a[index].contentDocument;
		var svgItem = svgDoc.getElementsByTagName("*"); //list all image elements
		for (i2 = 0; i2 < svgItem.length; ++i2) {
			var name=svgItem[i2].getAttribute("id"); 
			console.log("element:"+name);
			 if (oobdpatt.test(name)){//is the element a oobd map area
				console.log("passt: "+name);
				Oobd.addObject(svgItem[i2],"");
				svgItem[i2].setAttribute("style", "fill:#FF0000");
			}
		}
	}

    }
}, 10);


/**
* Oobd namespace.
*/
if (typeof Oobd == "undefined") {
	var Oobd = {
		/**
		* Initializes this object.
		*/
		connection:"",
		visualizers : new Array(),
		init : function(uri) {
			if ('WebSocket' in window){
				/* WebSocket is supported. You can proceed with your code*/
				this.connection = new WebSocket(uri);
				this.visualizers = new Array();
				this.connection.onopen = function(){
					/*Send a small message to the console once the connection is established */
					console.log('Connection open!');
				}

				this.connection.onclose = function(){
					console.log('Connection closed');
				}

				this.connection.onmessage = function(rawMsg){
					console.log("data "+rawMsg.data);
					try {
						var obj = JSON.parse(rawMsg.data);
						console.log(obj);
						console.log("type: "+obj.type);
						if (obj.type=="VALUE"){
							console.log("b64_value: "+obj.value);
							var server_message = "";
							if (typeof obj.value != "undefined"  && obj.value.length>0){
							  server_message = atob(obj.value);
							}
							var owner = obj.to.name;
							// Get all svg images, which are defined as OOBDMap class
							for (i = 0; i < Oobd.visualizers.length; ++i) { // search for the real id of a function owner
							  if (Oobd.visualizers[i].command==owner){
							    owner=Oobd.visualizers[i].name;
							    i=Oobd.visualizers.length; //break
							  }
							}
							
							var a = document.getElementsByClassName("OOBDMap");
							var oobdpatt = new RegExp("^oobd_"); // Regex pattern for svg area identification
							for (index = 0; index < a.length; ++index) { // go through all images found
								var svgDoc = a[index].contentDocument;
								var svgItem = svgDoc.getElementsByTagName("*"); //list all image elements
								for (i2 = 0; i2 < svgItem.length; ++i2) {
									var name=svgItem[i2].getAttribute("id"); 
									console.log("element:"+name);
									if (oobdpatt.test(name)){//is the element a oobd map area
										console.log("passt: "+name);
										Oobd.addObject(svgItem[i2],"");
										svgItem[i2].setAttribute("style", "fill:#FF0000");
									}
								}
							}

							console.log("owner:"+owner);
							var h= document.getElementById(owner);
							console.log("element:"+h.innerHTML);
							h.oobd.onion=obj;
							h.oodbupdate(obj);
						}
					}
					catch(err){
						console.log("Json Error "+err.message);
					}
				}
			} else {
				window.alert("Socket not supported");
			}
		},
		add : function(id, initialValue) { 
				var obj= document.getElementById(id);
				this.addObject(obj,initialValue);
		},

		splitID : function(id) { //splits the id into type and command
				return id.match(/oobd_(\w+)-(.+)/); 
		},

		addObject : function(obj, initialValue) {
				obj.oobd=new Object();
				obj.oobd.value=initialValue;
				thisElement = new Object();
				thisElement["name"] = obj.getAttribute("id");
				var params=this.splitID(obj.getAttribute("id"));
				console.log(params); 
				thisElement["type"] = params[1];
				thisElement["command"] = params[2];
				thisElement["object"]=obj;
				obj.oobd.command = params[2];
				this.visualizers.push(thisElement);
				obj.addEventListener("click", function(){
					console.log("clicked element "+this.id);
					console.log("clicked command "+obj.oobd.command);
					Oobd.connection.send("{'name'='"+obj.oobd.command+"','opt'='','value'='"+btoa(this.oobd.value)+"','type'=1}");
				});
		}
		
	}

}


