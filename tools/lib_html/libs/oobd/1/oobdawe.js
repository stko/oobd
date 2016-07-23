


/**
* Oobdawe namespace.
*/
if (typeof Oobdawe == "undefined") {
	var Oobdawe = {
		/**
		* Initializes this object.
		*/
		// custom global variables
		oobdawe_targetList : [],


		make3DText : function (id, message, text3dparams) {
			if (text3dparams === undefined) text3dparams = {};
			text3dparams["size"] 		= text3dparams.hasOwnProperty("size")		? text3dparams["size"] : 0.3;		// size of the text
			text3dparams["height"] 		= text3dparams.hasOwnProperty("height") 	? text3dparams["height"] : 0.05;	// thickness to extrude text
			text3dparams["curveSegments"]	= text3dparams.hasOwnProperty("curveSegments")	? text3dparams["curveSegments"] : 2;	// number of points on the curves
			text3dparams["font"] 		= text3dparams.hasOwnProperty("font")		? text3dparams["font"] : "helvetiker";	// font name
			text3dparams["weight"] 		= text3dparams.hasOwnProperty("weight")		? text3dparams["weight"] : "normal";	// font weight (normal, bold)
			text3dparams["style"] 		= text3dparams.hasOwnProperty("style")		? text3dparams["style"] : "normal"; 	// font style  (normal, italics)

			var material = new THREE.MeshBasicMaterial({color: 0xFF5555});
			var text3d_volume = new THREE.TextGeometry( message, text3dparams );
			var text3dItemV = new THREE.Mesh(text3d_volume, material); 
			text3dItemV.name = id;
			return text3dItemV;
		},

		
		raycaster : new THREE.Raycaster(),

		onDocumentMouseDown : function (event) {
			// the following line would stop any other event handler from firing
			// (such as the mouse's TrackballControls)
			// event.preventDefault();
			// update the mouse variable
			mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
			mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;
			// find intersections
			Oobdawe.raycaster.setFromCamera(mouse, camera);
			var intersects = Oobdawe.raycaster.intersectObjects(Oobdawe.oobdawe_targetList);
			// if there is one (or more) intersections
			if (intersects.length > 0) {
				console.log("Click on " + intersects[0].object.name);
				var event = new MouseEvent('click', {
					'view': window,
					'bubbles': true,
					'cancelable': true
				});
				var cb = intersects[0].object.userData.htmlElement;
				var canceled = !cb.dispatchEvent(event);
			}
		},


		replaceSprite : function (oobdObject,color) {
			console.log("3DText update");
			var innerCoord = oobdObject["coord"];
			var outerCoord = innerCoord.clone().multiplyScalar(Oobdawe.outerRadius / innerCoord.length());
			var my3DText = Oobdawe.make3DText(oobdObject.getAttribute("id"));
			my3DText.position.set(outerCoord.x, outerCoord.y, outerCoord.z);
			scene.remove(oobdObject["sprite"]);
			Oobdawe.removeA(Oobdawe.oobdawe_targetList,oobdObject["sprite"]);
			my3DText.userData.htmlElement=oobdObject;
			scene.add(my3DText);
			oobdObject["sprite"]=my3DText;
			Oobdawe.oobdawe_targetList.push(my3DText);
		},

		
		removeA : function (arr) {
			var what, a = arguments, L = a.length, ax;
			while (L > 1 && arr.length) {
				what = a[--L];
				while ((ax= arr.indexOf(what)) !== -1) {
					arr.splice(ax, 1);
				}
			}
			return arr;
		},
		
		
		
		loadOobdObjects : function (oobdObjects) {
			for (var i = 0; i < oobdObjects.length; i++) {
				var my3DText = Oobdawe.makeTextSprite(oobdObjects[i].getAttribute("id"));
				my3DText.position.set(outerCoord.x, outerCoord.y, outerCoord.z);
				my3DText.userData.htmlElement=oobdObjects[i];
				scene.add(my3DText);
				oobdObjects[i]["sprite"]=my3DText;
				oobdObjects[i].oodbupdate= function(input){
					console.log("Update request");
					var value=input.value;
					Oobdawe.replaceSprite(this,value);
					
				};
				Oobdawe.oobdawe_targetList.push(my3DText);
				Oobd.addObject(oobdObjects[i],"i");

			}
		},


		init : function () {
			// when the mouse moves, call the given function
			document.addEventListener('mousedown', Oobdawe.onDocumentMouseDown, false);

		}
	}
}
