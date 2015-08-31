

var oobdObjects = new Array();

//oobdObjects[0] = new Object();
oobdObjects[0] = document.createElement("BUTTON");
oobdObjects[0].setAttribute("oobd:fc","dtcStatus:125b11_741");
oobdObjects[0].setAttribute("oobd:type","spritebutton");
oobdObjects[0].setAttribute("oobd:click","yes");
oobdObjects[0].setAttribute("id","BCM");
oobdObjects[0]["coord"] = new THREE.Vector3(1.10000, 0.20000, 0.30000);

oobdObjects[1] = document.createElement("BUTTON");
oobdObjects[1].setAttribute("oobd:fc","dtcStatus:125b11_721");
oobdObjects[1].setAttribute("oobd:type","spritebutton");
oobdObjects[1].setAttribute("oobd:click","yes");
oobdObjects[1].setAttribute("id","Cluster");
oobdObjects[1]["coord"] = new THREE.Vector3(1.10000, -1.70000, 0.30000);


oobdObjects[2] = document.createElement("BUTTON");
oobdObjects[2].setAttribute("oobd:fc","dtcStatus:125b11_7E0");
oobdObjects[2].setAttribute("oobd:type","spritebutton");
oobdObjects[2].setAttribute("oobd:click","yes");
oobdObjects[2].setAttribute("id","PCM");
oobdObjects[2]["coord"] = new THREE.Vector3(1.10000, -3.20000, -0.30000);
