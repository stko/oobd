


/**
* Oobdjqx namespace.
*/
if (typeof Oobdjqx == "undefined") {
	var Oobdjqx = {
		/**
		* Initializes this object.
		*/
		init : function() {
			if ('Oobd' in window){
				console.log('init OOBD_jqx');				
				$(function(){
					var a = $(".OOBD");
					for (index = 0; index < a.length; ++index) { // go through all images found
						var oobdElement = a[index];
						var type=oobdElement.getAttribute("oobd:type");
						var name=oobdElement.getAttribute("id"); 
						var initialValue=oobdElement.getAttribute("oobd:value"); 
						console.log("oobdtype:"+type);
						console.log("id:"+name);
						if (type=="jqx"){
							console.log($(oobdElement));
							oobdElement.oodbupdate= function(input){
								console.log("Updatefunction erreicht!"+parseInt(atob(input.value)));
								console.log($(this));
								//$(this).jqxGauge('value',parseInt(atob(input.value)));
								//$(this)["jqxGauge"]('value',parseInt(atob(input.value)));
								$(this).val(parseInt(atob(input.value)));
							};
							Oobd.addObject(oobdElement,initialValue);
							//$('#gaugeContainer').jqxGauge('value',100);
						}
					}
				});
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


