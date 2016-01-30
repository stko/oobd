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
		wsURL: "ws://localhost:8443",
		connection: "",
		timerFlag: -1,
		timerObject: null,
		visualizers: new Array(),
		parseXml: null,
		fileSystem: null,
		onInitFs: null,
		onInitFserrorHandler: null,
		bufferName: "display",
		fsBufferArray: new Array(),
		fsBufferCounter: 0,
		init: function(uri) {
			// preparing utility funktion parse xmlDoc
			if (window.DOMParser) {
				this.parseXml = function(xmlStr) {
					return (new window.DOMParser()).parseFromString(xmlStr, "text/xml");
				};
			} else if (typeof window.ActiveXObject != "undefined" && new window.ActiveXObject("Microsoft.XMLDOM")) {
				this.parseXmlparseXml = function(xmlStr) {
					var xmlDoc = new window.ActiveXObject("Microsoft.XMLDOM");
					xmlDoc.async = "false";
					xmlDoc.loadXML(xmlStr);
					return xmlDoc;
				};
			} else {
				this.parseXmlparseXml = function() {
					return null;
				}
			}
			// try to disconnect the websocket as much early as possible before the new page is loaded
			window.addEventListener("unload", function() {
					/*Send a small message to the console once the connection is established */
					console.log('try to close Websocket');
					if (typeof Oobd.connection != "undefined"){
						Oobd.connection.close();
						console.log('Websocket closed');

					}
				});
			this.scanDOM();
			// try to init the file system
			Oobd.onInitFs = function onInitFs(fs) {
				console.log('Opened file system: ' + fs.name);
				Oobd.fileSystem=fs;
			};
			Oobd.onInitFserrorHandler = function errorHandler(e) {
				console.log('FileSystem '+e.name +'  ' + e.message);
			}
			// trying to be compatible
			window.URL = window.URL || window.webkitURL;
			window.resolveLocalFileSystemURL = window.resolveLocalFileSystemURL || window.webkitResolveLocalFileSystemURL || window.resolveLocalFileSystemURI;
			window.BlobBuilder = window.WebKitBlobBuilder || window.MozBlobBuilder || window.BlobBuilder;
			window.requestFileSystem  = window.requestFileSystem || window.webkitRequestFileSystem;
			window.requestFileSystem(window.TEMPORARY, 5*1024*1024 /*5MB*/, Oobd.onInitFs, Oobd.onInitFserrorHandler);
		},
		_handleWriteString: function(msg){
			var data="";
			var modifier="";
			if (typeof msg.modifier != "undefined" && msg.modifier.length > 0){
				modifier=decodeURIComponent(escape(atob(msg.modifier))).toLowerCase();
			}
			if (typeof msg.data != "undefined" && msg.data.length > 0){
				data=decodeURIComponent(escape(atob(msg.data)));
			}
			switch (modifier){
				case "setbuffer":
					Oobd.bufferName=data.toLowerCase();
				break;
				case "save":
				case "saveas":
					if (Oobd.bufferName == "display" ){ // a normal writestring
						if (typeof Oobd.writeString != "undefined"){
							console.log("try to WRITESTRING");
							console.log(msg);
							Oobd.writeString(data,"save");
						}
					}else{ // savefile
							if(typeof Oobd.fileSystem != "undefined"){
								//do we have the actual buffer already?
								var actBufferIndex=Oobd.fsBufferArray[Oobd.bufferName];
								if (typeof actBufferIndex != "undefined"){
									Oobd.fileSystem.root.getFile(actBufferIndex, {create: false}, function(fileEntry) {
										console.log('Try to download'+fileEntry.toURL());
										var a = document.createElement('a');
										a.download = data;
										a.href = fileEntry.toURL();
										a.textContent = "Save: "+Oobd.bufferName;
										a.click();

									}, Oobd.onInitFserrorHandler);
								}
							}
					}
				break;
				case "clear":
					if (Oobd.bufferName == "display" ){ // a normal writestring
						if (typeof Oobd.writeString != "undefined"){
							console.log("try to WRITESTRING");
							console.log(msg);
							Oobd.writeString(data,"clear");
						}
					}else{ // delete
							if(typeof Oobd.fileSystem != "undefined"){
								//do we have the actual buffer already?
								var thisBufferName=Oobd.bufferName;
								var actBufferIndex=Oobd.fsBufferArray[thisBufferName];
								if (typeof actBufferIndex != "undefined"){
									Oobd.fileSystem.root.getFile(actBufferIndex, {create: false}, function(fileEntry) {
										console.log('Try delete buffer '+ thisBufferName + " with index "+fileEntry.name);
										fileEntry.remove(function() {
											console.log('File removed.');
											delete Oobd.fsBufferArray[thisBufferName];
										}, Oobd.onInitFserrorHandler);

									}, Oobd.onInitFserrorHandler);
								}
							}
					}
				break;
				default:
					if (typeof data != "undefined" && data.length > 0){
						if (Oobd.bufferName == "display" ){ // a normal writestring
							if (typeof Oobd.writeString != "undefined"){
								console.log("try to WRITESTRING");
								console.log(msg);
								Oobd.writeString(data + "\n","");
							}
						}else{ // writetofile
							console.log("WRITESTRING modifier "+modifier);
							if(typeof Oobd.fileSystem != "undefined"){
								//do we have the actual buffer already?
								var actBufferIndex=Oobd.fsBufferArray[Oobd.bufferName];
								if (typeof actBufferIndex == "undefined"){
									actBufferIndex= Oobd.fsBufferCounter++;
									Oobd.fsBufferArray[Oobd.bufferName]=actBufferIndex;
									console.log('new Buffer "'+Oobd.bufferName+'" created with index:'+actBufferIndex);
								}
								Oobd.fileSystem.root.getFile(actBufferIndex, {create: true}, function(fileEntry) {

									// Create a FileWriter object for our FileEntry (log.txt).
									fileEntry.createWriter(function(fileWriter) {
										fileWriter.fe=fileEntry;
										fileWriter.onwriteend = function(e) {
											var src = e.target.fe.toURL();
											var src2 = fileEntry.toURL();
											console.log('Write completed with URL'+src2);
										};

										fileWriter.onerror = function(e) {
											console.log('Write failed: ' + e.toString());
										};
										fileWriter.seek(fileWriter.length); // Start write position at EOF.
										// Create a new Blob and write it to log.txt.
										window.BlobBuilder = window.BlobBuilder || window.WebKitBlobBuilder;// Note: window.WebKitBlobBuilder in Chrome 12.
										var blob = new Blob([data], {type: 'application/octet-stream'});
										fileWriter.write(blob);
									}, Oobd.onInitFserrorHandler);
								}, Oobd.onInitFserrorHandler);
							}
						}
					}
				}
			
			
			
			
			
			
			
			
			
			
			
		},
		scanDOM: function() {
			var a = document.getElementsByClassName("OOBD");
			for (index = 0; index < a.length; ++index) { // go through all images found
				var oobdElement = a[index];
				var type = oobdElement.getAttribute("oobd:type");
				var initialValue = oobdElement.getAttribute("oobd:value");
				if (type == "te") {
					oobdElement.oodbupdate = function(input) {
						this.getElementsByClassName("value")[0].innerHTML = input.value;
					};
					Oobd.addObject(oobdElement, initialValue);
				}
			}
			// Get all svg images, which are defined as OOBDMap class
			var a = document.getElementsByClassName("OOBDMap");
			for (index = 0; index < a.length; ++index) { // go through all images found
				var svgDoc = a[index].contentDocument;
				var svgItem = svgDoc.getElementsByClassName("oobd");
				for (i2 = 0; i2 < svgItem.length; ++i2) {
					var type = svgItem[i2].getAttribute("oobd:type");
					var fc = svgItem[i2].getAttribute("oobd:fc");
					var initialValue = svgItem[i2].getAttribute("oobd:value");
					Oobd.addObject(svgItem[i2], initialValue);
				}
			}
		},
		update: function() {
			for (i = 0; i < Oobd.visualizers.length; ++i) {
				console.log("update:" + Oobd.visualizers[i].command);
				Oobd.sendUpdateReq(Oobd.visualizers[i].command, Oobd.visualizers[i].optid, Oobd.visualizers[i].value, 1);
			}
		},
		_timerTick: function() {
			if (Oobd.timerFlag > -1) {
				if (Oobd.timerObject != null) window.clearTimeout(Oobd.timerObject);
				if (Oobd.timerFlag < Oobd.visualizers.length) {
					console.log("timer update:" + Oobd.visualizers[Oobd.timerFlag].command);
					Oobd.sendUpdateReq(Oobd.visualizers[Oobd.timerFlag].command, Oobd.visualizers[Oobd.timerFlag].optid, Oobd.visualizers[Oobd.timerFlag].value, 2);
					Oobd.timerFlag++;
				}
				if (Oobd.timerFlag == Oobd.visualizers.length) {
					Oobd.timerFlag = 0;
				}
				Oobd.timerObject = window.setTimeout(Oobd._timerTick, 1000);
			}
		},
		timer: function(on) {
			if (on) {
				this.timerFlag = 0;
				this._timerTick();
			} else {
				window.clearTimeout(this.timerObject);;
				this.timerFlag = -1;
			}
		},
		clearVisualiers: function() {
			this.visualizers = new Array();
		},
		start: function(webSocketURL) {
			if ('WebSocket' in window) {
				/* WebSocket is supported. You can proceed with your code*/
				if (typeof webSocketURL == "undefined") {
					webSocketURL=this.wsURL;
				}
				this.connection = new WebSocket(webSocketURL);
				this.connection.onopen = function() {
					/*Send a small message to the console once the connection is established */
					console.log('Connection open!');
				}

				this.connection.onclose = function() {
					console.log('Connection closed');
				}

				this.connection.onmessage = function(rawMsg) {
					console.log("data " + rawMsg.data);
					try {
						var obj = JSON.parse(rawMsg.data);
						if (obj.type == "VALUE") {
							obj.value = atob(obj.value);
							var owner = obj.to.name;
							for (i = 0; i < Oobd.visualizers.length; ++i) { // search for the real id of a function owner
								if (Oobd.visualizers[i].command == owner) {
									Oobd.visualizers[i].object.oobd.value = obj.value;
									Oobd.visualizers[i].object.oodbupdate(obj);
								}
							}
							Oobd._timerTick();
						}

						if (obj.type == "WSCONNECT") {
							if (typeof Oobd.onConnect != "undefined") {
								Oobd.onConnect();
							}
						}

						if (obj.type == "WRITESTRING") {
							Oobd._handleWriteString(obj);
						}

						if (obj.type == "PAGE") {
							if (typeof Oobd.openPage != "undefined" && typeof obj.name != "undefined" && obj.name.length > 0) {
								console.log("try to OpenPage");
								// bizarre UTF-8 decoding...
								// for later, if name comes base64encoded: Oobd.openPage(decodeURIComponent(escape(atob(obj.name))) + "\n");
								Oobd.openPage(decodeURIComponent(escape(obj.name)));
							}
						}
						
						if (obj.type == "VISUALIZE") {
							if (typeof Oobd.visualize != "undefined" ) {
								console.log("try to Visualize");
								Oobd.visualize(obj);
							}
						}
						if (obj.type == "PARAM") {
							if (typeof Oobd.alert != "undefined" ) {
								console.log("try to open Alert");
								Oobd.visualize(obj);
							}else{
								window.alert(obj.PARAM.tooltip);
								Oobd.connection.send('{"type":"PARAM","answer":""}');
							}
						}
						
						if (obj.type == "PAGEDONE") {
							if (typeof Oobd.pageDone != "undefined" ) {
								console.log("Page done");
								// bizarre UTF-8 decoding...
								// for later, if name comes base64encoded: Oobd.openPage(decodeURIComponent(escape(atob(obj.name))) + "\n");
								Oobd.pageDone();
							}
						}
						
						
					} catch (err) {
						console.log("received msg Error " + err.message);
					}
				}
			} else {
				window.alert("Socket not supported");
			}
		},
		add: function(id, initialValue) {
			var obj = document.getElementById(id);
			this.addObject(obj, initialValue);
		},

		splitID: function(id) { //splits the id into type and command
			return id.match(/oobd_(\w+)-(.+)/);
		},

		parseXml: function(xmlText) {
			if (window.DOMParser)
			{
				parser=new DOMParser();
				xmlDoc=parser.parseFromString(xmlText,"text/xml");
			}
			else // Internet Explorer
			{
				xmlDoc=new ActiveXObject("Microsoft.XMLDOM");
				xmlDoc.async=false;
				xmlDoc.loadXML(xmlText);
				return xmlDoc;
			}
		},
		loadXML: function(path, callback) {
			var request;

			// Create a request object. Try Mozilla / Safari method first.
			if (window.XMLHttpRequest) {
				request = new XMLHttpRequest();

				// If that doesn't work, try IE methods.
			} else if (window.ActiveXObject) {
				try {
					request = new ActiveXObject("Msxml2.XMLHTTP");
				} catch (e1) {
					try {
						request = new ActiveXObject("Microsoft.XMLHTTP");
					} catch (e2) {}
				}
			}

			// If we couldn't make one, abort.
			if (!request) {
				return;
			}

			// Upon completion of the request, execute the callback.
			request.onreadystatechange = function() {
				if (request.readyState === 4) {
					if (request.status === 200) {
						callback(request.responseText);
					}
				}
			};

			request.open("GET", path);
			try {
				req.responseType = "msxml-document"
			} catch (ex) {}
			request.send();
		},


		createXsltTransformer: function(path, callback) {

			Oobd.loadXML(path, function(xslText) {
				// xml contains the desired xml document.
				// do something useful with it!
				var xsl = Oobd.parseXml(xslText);

				// code for Chrome, Firefox, Opera, etc.
				if (document.implementation && document.implementation.createDocument) {
					var xsltProcessor = new XSLTProcessor();
					xsltProcessor.importStylesheet(xsl);
					callback(xsltProcessor);
				}
			});
		},



		sendUpdateReq: function(name, optid, value, updType) {
			if (typeof Oobd.connection.send != "undefined") {
				Oobd.connection.send('{"name":"' + name + '","optid":"' + optid + '","actValue":"' + btoa(value) + '","updType":' + updType + '}');
			} else {
				return false;
			}

		},

		addObject: function(obj, initialValue) {
			if (obj.getAttribute("oobd:fc") == null) {
				console.log("Error: Object with id " + obj.getAttribute("id") + " does not contain a functioncall (fc) attribut!");
			} else {
				obj.oobd = new Object();
				obj.oobd.value = initialValue;
				thisElement = new Object();
				thisElement["name"] = obj.getAttribute("id");
				thisElement["value"] = initialValue;
				thisElement["type"] = obj.getAttribute("oobd:type");
				thisElement["command"] = obj.getAttribute("oobd:fc");
				var params = obj.getAttribute("oobd:fc").match(/(\w+):(.+)/)
				if (params != null && params.length > 2) {
					thisElement["optid"] = params[2];
				} else {
					thisElement["optid"] = "";
				}
				thisElement["object"] = obj;
				obj.oobd.command = thisElement["command"];
				obj.oobd.optid = thisElement["optid"];
				Oobd.visualizers.push(thisElement);
				console.log("add object as visualizer:" + thisElement["command"]);
				if (obj.getAttribute("oobd:click") == "yes") {
					obj.addEventListener("click", function() {
						console.log("clicked command " + this.oobd.command + "with value" + this.oobd.value);
						//							Oobd.connection.send('{"name":"'+this.oobd.command+'","optid":"'+this.oobd.optid+'","value":"'+btoa(this.oobd.value)+'","updType":1}');
						Oobd.sendUpdateReq(this.oobd.command, this.oobd.optid, this.oobd.value, 0);
					});
				}
			}
		}

	}

}
