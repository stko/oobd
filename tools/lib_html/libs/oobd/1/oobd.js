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

							if (typeof Oobd.writeString != "undefined" && typeof obj.data != "undefined" && obj.data.length > 0) {
								console.log("try to WRITESTRING");
								// bizarre UTF-8 decoding...
								Oobd.writeString(decodeURIComponent(escape(atob(obj.data))) + "\n");
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
						Oobd.sendUpdateReq(this.oobd.command, this.oobd.optid, this.oobd.value, 1);
					});
				}
			}
		}

	}

}
