// custom global variables
var oobd_targetList = [];


function makeTextSprite(message, parameters) {
	if (parameters === undefined) parameters = {};

	var fontface = parameters.hasOwnProperty("fontface") ?
		parameters["fontface"] : "Arial";

	var fontsize = parameters.hasOwnProperty("fontsize") ?
		parameters["fontsize"] : 18;

	var borderThickness = parameters.hasOwnProperty("borderThickness") ?
		parameters["borderThickness"] : 4;

	var borderColor = parameters.hasOwnProperty("borderColor") ?
		parameters["borderColor"] : {
			r: 0,
			g: 0,
			b: 0,
			a: 1.0
		};

	var backgroundColor = parameters.hasOwnProperty("backgroundColor") ?
		parameters["backgroundColor"] : {
			r: 255,
			g: 255,
			b: 255,
			a: 1.0
		};


	var canvas = document.createElement('canvas');
	var context = canvas.getContext('2d');
	context.font = "Bold " + fontsize + "px " + fontface;

	// get size data (height depends only on font size)
	var metrics = context.measureText(message);
	var textWidth = metrics.width;
	canvas.width = textWidth + 2 * borderThickness;
	canvas.height = fontsize * 1.4 + 2 * borderThickness;

	context = canvas.getContext('2d');
	context.font = "Bold " + fontsize + "px " + fontface;
	var metrics = context.measureText(message);
	var textWidth = metrics.width;




	// background color
	context.fillStyle = "rgba(" + backgroundColor.r + "," + backgroundColor.g + "," + backgroundColor.b + "," + backgroundColor.a + ")";
	// border color
	context.strokeStyle = "rgba(" + borderColor.r + "," + borderColor.g + "," + borderColor.b + "," + borderColor.a + ")";

	context.lineWidth = borderThickness;
	roundRect(context, borderThickness / 2, borderThickness / 2, textWidth + borderThickness, fontsize * 1.4 + borderThickness, 6);
	// 1.4 is extra height factor for text below baseline: g,j,p,q.

	// text color
	context.fillStyle = "rgba(0, 0, 0, 1.0)";

	context.fillText(message, borderThickness, fontsize + borderThickness);

	// canvas contents will be used for a texture
	var texture = new THREE.Texture(canvas)
	texture.needsUpdate = true;

	var spriteMaterial = new THREE.SpriteMaterial({
		map: texture,
		useScreenCoordinates: false
	});
	var sprite = new THREE.Sprite(spriteMaterial);
	//sprite.scale.set(100,50,1.0);
	sprite.name = message;
	return sprite;
}

// function for drawing rounded rectangles
function roundRect(ctx, x, y, w, h, r) {
	ctx.beginPath();
	ctx.moveTo(x + r, y);
	ctx.lineTo(x + w - r, y);
	ctx.quadraticCurveTo(x + w, y, x + w, y + r);
	ctx.lineTo(x + w, y + h - r);
	ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
	ctx.lineTo(x + r, y + h);
	ctx.quadraticCurveTo(x, y + h, x, y + h - r);
	ctx.lineTo(x, y + r);
	ctx.quadraticCurveTo(x, y, x + r, y);
	ctx.closePath();
	ctx.fill();
	ctx.stroke();
}

var raycaster = new THREE.Raycaster();

function onDocumentMouseDown(event) {
	// the following line would stop any other event handler from firing
	// (such as the mouse's TrackballControls)
	// event.preventDefault();

	// update the mouse variable
	mouse.x = (event.clientX / window.innerWidth) * 2 - 1;
	mouse.y = -(event.clientY / window.innerHeight) * 2 + 1;



	// find intersections

	raycaster.setFromCamera(mouse, camera);

	var intersects = raycaster.intersectObjects(oobd_targetList);


	// if there is one (or more) intersections
	if (intersects.length > 0) {
		//		intersects[0].object.geometry.computeBoundingBox();
		console.log("Click on " + intersects[0].object.name);
	}

}


// when the mouse moves, call the given function
document.addEventListener('mousedown', onDocumentMouseDown, false);




var outerRadius = 5.0;


function oobdObjectsInit() {


	for (var i = 0; i < oobdObjects.length; i++) {
		var geometry = new THREE.SphereGeometry(0.1, 32, 32);
		var material = new THREE.MeshBasicMaterial({
			color: 0xffff00
		});
		var sphere = new THREE.Mesh(geometry, material);
		var innerCoord = oobdObjects[i]["coord"];
		var outerCoord = innerCoord.clone().multiplyScalar(outerRadius / innerCoord.length());
		sphere.position.set(innerCoord.x, innerCoord.y, innerCoord.z);
		scene.add(sphere);


		var spritey = makeTextSprite(oobdObjects[i]["name"], {
			fontsize: 20,
			borderColor: {
				r: 255,
				g: 0,
				b: 0,
				a: 1.0
			},
			backgroundColor: {
				r: 255,
				g: 100,
				b: 100,
				a: 0.8
			}
		});
		spritey.position.set(outerCoord.x, outerCoord.y, outerCoord.z);
		scene.add(spritey);
		oobd_targetList.push(spritey);

		var lineGeometry = new THREE.Geometry();
		var vertArray = lineGeometry.vertices;
		vertArray.push(innerCoord, outerCoord);
		lineGeometry.computeLineDistances();
		var lineMaterial = new THREE.LineBasicMaterial({
			color: 0xcc0000
		});
		var line = new THREE.Line(lineGeometry, lineMaterial);
		scene.add(line);

	}


	for (var i = 0; i < 10; i++) {
		var spritey = makeTextSprite("x" + i, {
			fontsize: 10,
			borderColor: {
				r: 255,
				g: 0,
				b: 0,
				a: 1.0
			},
			backgroundColor: {
				r: 255,
				g: 100,
				b: 100,
				a: 0.8
			}
		});
		spritey.position.set(i, 0, 0);
		scene.add(spritey);

	}

	for (var i = 0; i < 10; i++) {
		var spritey = makeTextSprite("y" + i, {
			fontsize: 10,
			borderColor: {
				r: 255,
				g: 0,
				b: 0,
				a: 1.0
			},
			backgroundColor: {
				r: 255,
				g: 100,
				b: 100,
				a: 0.8
			}
		});
		spritey.position.set(0, i, 0);
		scene.add(spritey);

	}


	for (var i = 0; i < 10; i++) {
		var spritey = makeTextSprite("z" + i, {
			fontsize: 10,
			borderColor: {
				r: 255,
				g: 0,
				b: 0,
				a: 1.0
			},
			backgroundColor: {
				r: 255,
				g: 100,
				b: 100,
				a: 0.8
			}
		});
		spritey.position.set(0, 0, i);
		scene.add(spritey);

	}


}