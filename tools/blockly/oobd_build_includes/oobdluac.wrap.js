      function download(strData, strFileName, strMimeType) {
      		var D = document,
      			A = arguments,
      			a = D.createElement("a"),
      			d = A[0],
      			n = A[1],
      			t = A[2] || "text/plain";

      		//build download link:
      		a.href = "data:" + strMimeType + "," + escape(strData);

      		if ('download' in a) {
      			a.setAttribute("download", n);
      			a.innerHTML = "downloading...";
      			D.body.appendChild(a);
      			setTimeout(function() {
      				var e = D.createEvent("MouseEvents");
      				e.initMouseEvent(
      					"click", true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null
      				);
      				a.dispatchEvent(e);
      				D.body.removeChild(a);
      			}, 66);
      			return true;
      		}; //end if a[download]?

      		//do iframe dataURL download:
      		var f = D.createElement("iframe");
      		D.body.appendChild(f);
      		f.src = "data:" + strMimeType + ";base64," + escape(strData);
      		setTimeout(function() {
      			D.body.removeChild(f);
      		}, 333);
      		return true;
      	} //end download()


      //example usage:
      //download("hello world", "test.txt", "text/plain")
      //download( result.chars.join(""), report.pdf", "application/pdf");


     function doExecute() {

      function ArrayBufferToString(buffer) {
      	return BinaryToString(String.fromCharCode.apply(null, Array.prototype.slice.apply(new Uint8Array(buffer))));
      }

      function StringToArrayBuffer(string) {
      	return StringToUint8Array(string).buffer;
      }

      function BinaryToString(binary) {
      	var error;

      	try {
      		return decodeURIComponent(escape(binary));
      	} catch (_error) {
      		error = _error;
      		if (error instanceof URIError) {
      			return binary;
      		} else {
      			throw error;
      		}
      	}
      }

	function _base64ToArrayBuffer(base64) {
		var binary_string =  window.atob(base64);
		var len = binary_string.length;
		var bytes = new Uint8Array( len );
		for (var i = 0; i < len; i++)        {
			bytes[i] = binary_string.charCodeAt(i);
		}
		return bytes.buffer;
	}

      var Module = {
      	arguments: ['lualib.lua', 'dummy.lua'],
      	postRun: function() {
      		var fileResult = FS.readFile('luac.out');
      		// zwei Arrays zusammentackern: a.push.apply(a, b);
      		download(ArrayBufferToString(fileResult), "OOBDWeb.lbc", "application/octet-stream");
      		console.log('postrun');
      	},
      	'preRun': [function() {
      		FS.writeFile('dummy.lua', document.luaSource, { encoding: "utf8" });
      		console.log('prerun with source:' +document.luaSource);
      	}],
      	'calledRun': false,
      	ABORT: false,

      	print: (function() {
      		var element = document.getElementById('importExport');
      		// element.value = ''; // clear browser cache
      		return function(text) {
      			text = Array.prototype.slice.call(arguments).join(' ');
      			// These replacements are necessary if you render to raw HTML
      			//text = text.replace(/&/g, "&amp;");
      			//text = text.replace(/</g, "&lt;");
      			//text = text.replace(/>/g, "&gt;");
      			//text = text.replace('\n', '<br>', 'g');
      			element.value += text + "\n";
      			element.scrollTop = element.scrollHeight; // focus on bottom
      		};
      	})(),
      	printErr: function(text) {
      		text = Array.prototype.slice.call(arguments).join(' ');
      		if (0) { // XXX disabled for safety typeof dump == 'function') {
      			dump(text + '\n'); // fast, straight to the real console
      		} else {
      			console.log(text);
      		}
      	},
//       	canvas: document.getElementById('canvas'),
//       	setStatus: function(text) {
//       		if (!Module.setStatus.last) Module.setStatus.last = {
//       			time: Date.now(),
//       			text: ''
//       		};
//       		if (text === Module.setStatus.text) return;
//       		var m = text.match(/([^(]+)\((\d+(\.\d+)?)\/(\d+)\)/);
//       		var now = Date.now();
//       		if (m && now - Date.now() < 30) return; // if this is a progress update, skip it if too soon
//       		if (m) {
//       			text = m[1];
//       			progressElement.value = parseInt(m[2]) * 100;
//       			progressElement.max = parseInt(m[4]) * 100;
//       			progressElement.hidden = false;
//       			spinnerElement.hidden = false;
//       		} else {
//       			progressElement.value = null;
//       			progressElement.max = null;
//       			progressElement.hidden = true;
//       			if (!text) spinnerElement.hidden = true;
//       		}
//       		statusElement.innerHTML = text;
//       	},
      	totalDependencies: 0,
      	monitorRunDependencies: function(left) {
      		this.totalDependencies = Math.max(this.totalDependencies, left);
      		Module.setStatus(left ? 'Preparing... (' + (this.totalDependencies - left) + '/' + this.totalDependencies + ')' : 'All downloads complete.');
      	}
      };
      //Module.setStatus('Downloading...');

 	      
