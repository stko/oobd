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
		wsURL: "ws://"+window.location.hostname+":8443",
		alreadyInitialized: false,
		session: null,
		connection: "",
		scriptID: "",
		updateNormalMarker: false,
		updateTimerMarker: false,
		timerFlag: -1,
		timerObject: null,
		visualizers: new Array(),
		parseXml: null,
		fileSystem: null,
		onInitFs: null,
		onInitFserrorHandler: null,
		bufferName: "display",
		fsBufferArray: new Object(),
		fsBufferCounter: 0,
		isTouchDevice : 'ontouchstart' in document.documentElement,
		uniqueID: 0,
		getUniqueID: function(){
			try {
				Oobd.uniqueID++;
			} catch (ex) {
				Oobd.uniqueID=0;
			}
			return Oobd.uniqueID;
		},
		FLAG_SUBMENU : 1,
		FLAG_UPDATE  : 2,
		FLAG_TIMER   : 4,
		FLAG_LOG     : 8,
		FLAG_BACK    : 16,
		FLAG_GRAPH   : 32,
		setUpdateFlag: function(obj,flagID,flag){
			var viz=obj.oobd.visualizer;
			var flagValue = viz.updevents;
			if(flag){
				flagValue = flagValue | flagID;
			}else{
				flagValue = flagValue ^ flagID;
			}
			viz.updevents=flagValue;
		},
		getUpdateFlag: function(viz,flagID){
			return  (viz.updevents & flagID) != 0;
		},
		loadSession: function(){
			if (Oobd.session==null){
				Oobd.session = JSON.parse(localStorage.getItem('session'));
			};
			if (Oobd.session==null){
					Oobd.session = {
					dashboard : [],
					connectType : "",
					pgpid : "",
					theme : "",
					connectID : {}
				}
			}
		},
		loadUserPrefs: function (connectTypeSelect, pgpidInput, themeSelect, connectIDInput){
			Oobd.loadSession();
			if (Oobd.session.connectType!=""){
				connectTypeSelect.value=Oobd.session.connectType;
			}
			if (Oobd.session.pgpid!=""){
				pgpidInput.value=Oobd.session.pgpid;
			}
			if (Oobd.session.theme!=""){
				themeSelect.value=Oobd.session.theme;
			}
			if (typeof Oobd.session.connectID[Oobd.session.connectType] != "undefined" && Oobd.session.connectID[Oobd.session.connectType]!=""){
				connectIDInput.value=Oobd.session.connectID[Oobd.session.connectType];
			}
		},
		saveUserPrefs: function (connectTypeSelect, pgpidInput, themeSelect, connectIDInput){
			Oobd.session.connectType=connectTypeSelect.value;
			Oobd.session.pgpid=pgpidInput.value;
			Oobd.session.theme=themeSelect.value;
			Oobd.session.connectID[Oobd.session.connectType]=connectIDInput.value;
			localStorage.setItem('session', JSON.stringify(Oobd.session));
		},

		init: function(uri) {
			if (Oobd.alreadyInitialized){
				return;
			}
			Oobd.alreadyInitialized=true;
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
			Oobd.loadSession();
		},
		_announceFileChange: function(id,url,change){
			console.log("Announce File change: " + change + " for id " + id + " with URL "+ url );
			if (typeof Oobd.fileChange != "undefined"){
				Oobd.fileChange(id,url,change);
			}
		},
		addToDash: function(constructor){
			Oobd.session.dashboard.push({ 'name': constructor, 'file': Oobd.scriptID });
			localStorage.setItem('session', JSON.stringify(Oobd.session));
			return Oobd.getDashContructors();
		},
		removeFromDash: function(constructor){
			var index =-1
			for (var i = 0; i < Oobd.session.dashboard.length; i++) {
				if (Oobd.session.dashboard[i].file==Oobd.scriptID){
					if (Oobd.session.dashboard[i].name==constructor){
						index=i;
					}
				}
			}
			if (index > -1) {
				Oobd.session.dashboard.splice(index, 1);
			}
			localStorage.setItem('session', JSON.stringify(Oobd.session));
			return Oobd.getDashContructors();
		},
		getDashContructors: function(){
			Oobd.loadSession();
			var res=[];
			for (var i = 0; i < Oobd.session.dashboard.length; i++) {
				if (Oobd.session.dashboard[i].file==Oobd.scriptID){
					res.push(Oobd.session.dashboard[i].name);
				}
			}
			return res;
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
			console.log("File handler called with modifier >"+modifier+"< and data >"+data+"<");
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
				case "close":
					if (Oobd.bufferName != "display" ){ // a normal writestring
							if(typeof Oobd.fileSystem != "undefined"){
								//do we have the actual buffer already?
								var thisBufferName=Oobd.bufferName;
								var actBufferIndex=Oobd.fsBufferArray[Oobd.bufferName];
								if (typeof actBufferIndex != "undefined"){
									console.log("try to close file "+thisBufferName );
									Oobd.fileSystem.root.getFile(actBufferIndex, {create: false}, function(fileEntry) {
										Oobd._announceFileChange(thisBufferName,fileEntry.toURL(),"close");

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
							Oobd.writeString(data,"cLlear");
						}
					}else{ // delete
							if(typeof Oobd.fileSystem != "undefined"){
								//do we have the actual buffer already?
								var thisBufferName=Oobd.bufferName;
								var actBufferIndex=Oobd.fsBufferArray[thisBufferName];
								if (typeof actBufferIndex != "undefined"){
									Oobd.fileSystem.root.getFile(actBufferIndex, {create: true}, function(fileEntry) {
										console.log('Try delete buffer '+ thisBufferName + " with index "+fileEntry.name);
										fileEntry.remove(function() {
											console.log('File removed:'+Oobd.bufferName + "/"+thisBufferName);
											delete Oobd.fsBufferArray[thisBufferName];
											Oobd._announceFileChange(thisBufferName,"","clear");
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
								var thisBufferName=Oobd.bufferName;
								var actBufferIndex=Oobd.fsBufferArray[thisBufferName];
								var isNewfile=false;
								if (typeof actBufferIndex == "undefined"){
									actBufferIndex= Oobd.fsBufferCounter++;
									Oobd.fsBufferArray[thisBufferName]=actBufferIndex;
									console.log('new Buffer "'+thisBufferName+'" created with index:'+actBufferIndex);
									isNewfile=true;
								}
								console.log("try to write to a file with bufferindex "+ actBufferIndex);
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
											console.log('Write failed on bufferindex '+actBufferIndex+': ' + e.toString());
										};
										console.log("FileWriter.length=",fileWriter.length);
										if (isNewfile){ // new file
											isNewfile=false;
											Oobd._announceFileChange(thisBufferName,fileEntry.toURL(),"create");
										}
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
			this.updateNormalMarker=true;
			this.timerFlag = 0;
			//if the timer not ticks already for timer updates
			if (this.timerObject == null){
				this._timerTick();
			}
		},
		_timerTick: function() {
			if (Oobd.timerFlag > -1) {
				var waitForNext=10; // in case we find nothing to update, we'll wait short before try the next
				if (Oobd.timerObject != null){
					window.clearTimeout(Oobd.timerObject);
					Oobd.timerObject=null;
				}
				if (Oobd.timerFlag < Oobd.visualizers.length) {
					console.log("check for update:"+Oobd.timerFlag);
					if (Oobd.updateNormalMarker){
						if (Oobd.getUpdateFlag(Oobd.visualizers[Oobd.timerFlag],Oobd.FLAG_UPDATE)){
							waitForNext=1000; // we found something to update, so'll have to wait longer (for an potential answer)
							console.log("update- update:" + Oobd.visualizers[Oobd.timerFlag].command);
							Oobd.sendUpdateReq(Oobd.visualizers[Oobd.timerFlag].command, Oobd.visualizers[Oobd.timerFlag].optid, Oobd.visualizers[Oobd.timerFlag].value, 2);
						}
					}else{
						if (Oobd.getUpdateFlag(Oobd.visualizers[Oobd.timerFlag],Oobd.FLAG_TIMER)){
							waitForNext=1000; // we found something to update, so'll have to wait longer (for an potential answer)
							console.log("timer update:" + Oobd.visualizers[Oobd.timerFlag].command);
							Oobd.sendUpdateReq(Oobd.visualizers[Oobd.timerFlag].command, Oobd.visualizers[Oobd.timerFlag].optid, Oobd.visualizers[Oobd.timerFlag].value, 2);
						}
					}
					Oobd.timerFlag++;
				}
				if (Oobd.timerFlag == Oobd.visualizers.length) {
					Oobd.timerFlag = Oobd.updateTimerMarker ? 0 : -1;
					Oobd.updateNormalMarker=false;
				}
			}
			if (Oobd.timerFlag > -1) {
				Oobd.timerObject = window.setTimeout(Oobd._timerTick, waitForNext);
			}
		},
		timer: function(on) {
			if (on) {
				this.updateTimerMarker=true;
				this.timerFlag = 0;
				if (this.timerObject == null){
					this._timerTick();
				}
			} else {
				if (this.timerObject != null && !this.updateNormalMarker){
					window.clearTimeout(this.timerObject);
					this.timerObject=null;
					this.timerFlag = -1;
				}
				this.updateTimerMarker=false;
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
									if (Oobd.visualizers[i].object.oobd.value != obj.value){
										if (Oobd.getUpdateFlag(Oobd.visualizers[i],Oobd.FLAG_LOG)){
											Oobd.writeString(new Date().toLocaleTimeString()+"\t"+Oobd.visualizers[i].tooltip + "\t"+Oobd.visualizers[i].object.oobd.value + "\t"+obj.value+"\n","");
										}
									}
									Oobd.visualizers[i].object.oobd.value = obj.value;
									Oobd.visualizers[i].object.oodbupdate(obj);
								}
							}
							Oobd._timerTick();
						}

						if (obj.type == "WSCONNECT") {
							Oobd.scriptID=obj.script;
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
								// as OpenPages resets the list of available visualizers also for the Dashboard, we have to redraw the dashboard after each new page
								if (typeof Oobd.fillDashboard != "undefined") {
									Oobd.fillDashboard();
								}
							}
						}
						
						if (obj.type == "VISUALIZE") {
							if (typeof Oobd.visualize != "undefined" ) {
								console.log("try to Visualize");
								Oobd.visualize(obj);
							}
						}
						if (obj.type == "PARAM") {
							if (typeof obj.PARAM.confirm !="undefined"){ // do we need a yes/no dialog or a value input?
								if (typeof Oobd.confirm != "undefined" ) {
									console.log("try to open confirm");
									Oobd.confirm(obj);
								}else{
									var answer = window.confirm(atob(obj.PARAM.text)) ? "true":"false";
									Oobd.connection.send('{"type":"PARAM","answer":"'+btoa(answer)+'"}');
								}
							}else{
								if (typeof Oobd.prompt != "undefined" ) {
									console.log("try to open prompt");
									Oobd.prompt(obj);
								}else{
									var answer = window.prompt(atob(obj.PARAM.text),atob(obj.PARAM.default));
									Oobd.connection.send('{"type":"PARAM","answer":"'+btoa(answer)+'"}');
								}
							}
						}
						if (obj.type == "DIALOG_INFO") {
							if (typeof Oobd.alert != "undefined" ) {
								console.log("try to open Alert");
								Oobd.alert(obj);
							}else{
								window.alert(atob(obj.DIALOG_INFO.tooltip));
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
				thisElement["updevents"] = obj.getAttribute("oobd:updevents");
				thisElement["tooltip"] = obj.getAttribute("oobd:tooltip");
				var params = obj.getAttribute("oobd:fc").match(/(\w+):(.+)/)
				if (params != null && params.length > 2) {
					thisElement["optid"] = params[2];
				} else {
					thisElement["optid"] = "";
				}
				thisElement["object"] = obj;
				obj.oobd.command = thisElement["command"];
				obj.oobd.optid = thisElement["optid"];
				obj.oobd.visualizer = thisElement;
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
