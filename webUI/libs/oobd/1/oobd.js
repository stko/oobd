

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
	// Get all elements, which are defined as oobd class
	var a = document.getElementsByClassName("OOBD");
	for (index = 0; index < a.length; ++index) { // go through all images found
		var oobdElement = a[index];
		var type=oobdElement.getAttribute("oobd:type");
		var name=oobdElement.getAttribute("id"); 
		console.log("oobdtype:"+type);
		console.log("id:"+name);
		if (type=="te"){
			oobdElement.oodbupdate= function(input){
				oobdElement.getElementsByClassName("value")[0].innerHTML = atob(input.value);
			};
			Oobd.addObject(oobdElement,"");
		}
	}

	// Get all svg images, which are defined as OOBDMap class
	var a = document.getElementsByClassName("OOBDMap");
	for (index = 0; index < a.length; ++index) { // go through all images found
		var svgDoc = a[index].contentDocument;
		var svgItem = svgDoc.getElementsByClassName("oobd");
		for (i2 = 0; i2 < svgItem.length; ++i2) {
			var type=svgItem[i2].getAttribute("oobd:type"); 
			var fc=svgItem[i2].getAttribute("oobd:fc"); 
			console.log("oobd:type:"+name);
			console.log("oobd:fc:"+fc);
			Oobd.addObject(svgItem[i2],"");
			svgItem[i2].oodbupdate("bla");
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
						if (obj.type=="WRITESTRING"){
						if (typeof Oobd.writeString != "undefined"  && typeof obj.value != "undefined" && obj.value.length>0){
							  console.log("output:"+obj.value);
							  Oobd.writeString(obj.value);
							}
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
				thisElement["type"] = obj.getAttribute("oobd:type");
				thisElement["command"] = obj.getAttribute("oobd:fc");
				console.log("id:"+thisElement["name"]); 
				console.log("type:"+thisElement["type"]); 
				console.log("fc:"+thisElement["command"]); 
				thisElement["object"]=obj;
				obj.oobd.command = params[2];
				this.visualizers.push(thisElement);
				obj.addEventListener("click", function(){
					console.log("clicked element "+this.id);
					console.log("clicked command "+obj.oobd.command);
					Oobd.connection.send("{'name'='"+obj.oobd.command+"','opt'='','value'='"+btoa(this.oobd.value)+"','type'=1}");
					this.getElementsByClassName("name")[0].innerHTML = "Name";
					this.getElementsByClassName("value")[0].innerHTML = "Wert";
				});
		}
		
	}

}


