


/**
* Oobd namespace.
*/
if (typeof Oobdjqx == "undefined") {
	var Oobdjqx = {
		/**
		* Initializes this object.
		*/
		init : function() {
			if ('Oobd' in window){
				console.log('init OOBD_jqx');
				var a = $(".OOBD");
				for (index = 0; index < a.length; ++index) { // go through all images found
					var oobdElement = a[index];
					var type=oobdElement.getAttribute("oobd:type");
					var name=oobdElement.getAttribute("id"); 
					console.log("oobdtype:"+type);
					console.log("id:"+name);
					if (type=="jqx"){
						console.log($(oobdElement));
						oobdElement.oodbupdate= function(input){
							console.log("Updatefunction erreicht!"+input.value);
							console.log(this);
							this.jqxGauge('value',120);
						};
						Oobd.addObject(oobdElement,"");
						//$(oobdElement).jqxGauge('value',120);
						 //$('#gaugeContainer').jqxGauge('value',120);
					}
				}
			} else {
				window.alert("Oobd base lib is missing!");
			}
		},
		add : function(id, initialValue) { 
				Oobd.addObject($(id)[0],initialValue);
		},


		addObject : function(obj, initialValue) {
				Oobd.addObject($(id)[0],initialValue);

		}
		
	}

}


