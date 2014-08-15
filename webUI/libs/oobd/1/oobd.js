/**
* Oobd namespace.
*/
if (typeof Oobd == "undefined") {
	var Oobd = {
		/**
		* Initializes this object.
		*/
		connection:"",
		init : function(aEvent) {
			if ('WebSocket' in window){
				/* WebSocket is supported. You can proceed with your code*/
				window.alert("Socket supported");
				//var connection = new WebSocket('ws://example.org:12345/myapp');
				this.connection.onopen = function(){
					/*Send a small message to the console once the connection is established */
					console.log('Connection open!');
				}

				this.connection.onclose = function(){
					console.log('Connection closed');
				}

				this.connection.onmessage = function(e){
					var server_message = e.data;
					console.log(server_message);
				}
			} else {
				/*WebSockets are not supported. Try a fallback method like long-polling etc*/
				window.alert("Socket not supported");
			}
		}
	}
	Oobd.init();

}





var message = {
	'name': 'bill murray',
	'comment': 'No one will ever believe you'
};
//Oobd.connection.send(JSON.stringify(message));

