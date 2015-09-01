


var readyStateCheckInterval = setInterval(function() {
    if (document.readyState === "complete") {
        clearInterval(readyStateCheckInterval);
	Oobd.init();
	// Get all elements, which are defined as oobd class
    }
}, 10);


/**
* Oobd namespace.
*/
if (typeof Oobd == "undefined") {
	Oobd = {
		/**
		* Initializes this object.
		*/
		wsURL:"ws://localhost:8443",
		connection:"",
		timerFlag:-1,
		timerObject: null,
		visualizers : new Array(),
		init : function(uri) {
			var a = document.getElementsByClassName("OOBD");
			for (index = 0; index < a.length; ++index) { // go through all images found
				var oobdElement = a[index];
				var type=oobdElement.getAttribute("oobd:type");
				var name=oobdElement.getAttribute("id"); 
				if (type=="te"){
					oobdElement.oodbupdate= function(input){
						this.getElementsByClassName("value")[0].innerHTML = atob(input.value);
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
					Oobd.addObject(svgItem[i2],"");
					// svgItem[i2].oodbupdate("bla");
				}
			}
		},
		update : function (){
				for (i = 0; i < Oobd.visualizers.length; ++i) { 
					console.log("update:"+Oobd.visualizers[i].command);
					Oobd.connection.send('{"name":"'+Oobd.visualizers[i].command+'","optid":"'+Oobd.visualizers[i].optid+'","value":"'+btoa(Oobd.visualizers[i].value)+'","updType":0}');
				}
		},
		_timerTick : function (){
				if (Oobd.timerFlag> -1){
					if (Oobd.timerObject != null) window.clearTimeout(Oobd.timerObject);
					if (Oobd.timerFlag < Oobd.visualizers.length){
						console.log("timer update:"+Oobd.visualizers[Oobd.timerFlag].command);
						Oobd.connection.send('{"name":"'+Oobd.visualizers[Oobd.timerFlag].command+'","optid":"'+Oobd.visualizers[Oobd.timerFlag].optid+'","value":"'+btoa(Oobd.visualizers[Oobd.timerFlag].value)+'","updType":2}');
						Oobd.timerFlag++;
					}
					if (Oobd.timerFlag == Oobd.visualizers.length){
						Oobd.timerFlag=0;
					}
					Oobd.timerObject=window.setTimeout(Oobd._timerTick, 1000);
				}
		},
		timer : function(on) {
			if (on){
				this.timerFlag=0;
				this._timerTick();
			}else{
				window.clearTimeout(this.timerObject);;
				this.timerFlag=-1;
			}
		},
		start : function() {
			if ('WebSocket' in window){
				/* WebSocket is supported. You can proceed with your code*/
				this.connection = new WebSocket(this.wsURL);
				//this.visualizers = new Array();
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
						if (obj.type=="VALUE"){
							var owner = obj.to.name;
							for (i = 0; i < Oobd.visualizers.length; ++i) { // search for the real id of a function owner
								if (Oobd.visualizers[i].command==owner){
									Oobd.visualizers[i].object.oobd.value=atob(obj.value);
									Oobd.visualizers[i].object.oodbupdate(obj);
								}
							}
							Oobd._timerTick();
						}
						if (obj.type=="WSCONNECT"){
							if (typeof Oobd.onConnect != "undefined"  ){
								Oobd.onConnect();
							}
						}
						if (obj.type=="WRITESTRING"){
							
							if (typeof Oobd.writeString != "undefined"  && typeof obj.data != "undefined" && obj.data.length>0){
								console.log("try to WRITESTRING");
								// bizarre UTF-8 decoding...
								Oobd.writeString(decodeURIComponent(escape(atob(obj.data)))+"\n");
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
				if (obj.getAttribute("oobd:fc")== null){
					console.log("Error: Object with id "+obj.getAttribute("id")+ " does not contain a functioncall (fc) attribut!"); 
				}else{
					obj.oobd=new Object();
					obj.oobd.value=initialValue;
					thisElement = new Object();
					thisElement["name"] = obj.getAttribute("id");
					thisElement["value"] = initialValue;
					thisElement["type"] = obj.getAttribute("oobd:type");
					thisElement["command"] = obj.getAttribute("oobd:fc");
					var params=obj.getAttribute("oobd:fc").match(/(\w+):(.+)/)
					if (params != null && params.length>2){
						thisElement["optid"] =params[2];
					}else{
						thisElement["optid"] ="";
					}
					thisElement["object"]=obj;
					obj.oobd.command = thisElement["command"];
					obj.oobd.optid = thisElement["optid"];
					Oobd.visualizers.push(thisElement);
					console.log("add object as visualizer:"+thisElement["command"]); 
					if (obj.getAttribute("oobd:click")=="yes"){
						obj.addEventListener("click", function(){
							console.log("clicked command "+this.oobd.command + "with value" + this.oobd.value);
							Oobd.connection.send('{"name":"'+this.oobd.command+'","optid":"'+this.oobd.optid+'","value":"'+btoa(this.oobd.value)+'","updType":1}');
						});
					}
				}
		}
		
	}

}


