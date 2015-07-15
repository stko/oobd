

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
						console.log("type: "+obj.type);
						if (obj.type=="VALUE"){
							console.log("b64_value: "+obj.value);
							var server_message = atob(obj.value);
							var owner = obj.to.name;
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
				var h= document.getElementById(id);
				h.oobd=new Object();
				h.oobd.value=initialValue;
				thisElement = new Object();
				thisElement["name"] = id;
				thisElement["object"]=h;
				this.visualizers.push(thisElement);
				h.addEventListener("click", function(){
					console.log("clicked element "+this.id);
					Oobd.connection.send("{'name'='"+this.id+"','opt'='','value'='"+btoa(this.oobd.value)+"','type'=1}");
				});
		}
		
	}

}


