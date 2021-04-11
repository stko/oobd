<template>
	<v-container>
		<h1>OOBD</h1>
		<v-select
			label="Select connection method"
			v-model="connection"
			:items="items"
			item-text="text"
			item-value="value"
			persistent-hint
			single-line
		></v-select>
		<br />
		<v-text-field
		label="Your WebSocket URL"
		v-model="WebSocketURL"
		v-if="connection=='websocket'">
		</v-text-field>
		<v-file-input
			label="Select your Lua Script"
			accept=".json"
			v-model="luaFileName" 
			required
		>
		</v-file-input>
		<v-btn block @click="start" :disabled="luaFileName.length == 0">
			Start
		</v-btn>
	</v-container>
</template>
<script>

import oobdpwa from "@/oobdpwa";
import router from "@/router";


export default {
	name: "Start",
	inject:[
		'text_dialog',
		'show_text_dialog'
	],
	data() {
		return {
			connection: "serial",
			WebSocketURL: "",
			items: [
				{ value: "serial", text: "use a serial Connection" },
				{ value: "websocket", text: "use a Websocket Connection" },
			],
			// File selectors must be initialized with []. If using a simple empty string "", it throws a Vue Vuetify: Invalid prop: custom validator check failed for prop “value”. found in ---> <VFileInput>
			luaFileName: [],
		};
	},
	methods: {
		async start() {
			console.log("start");
			console.log(this.luaFileName);
			var diag =await this.show_text_dialog('Titel in','nix')
			if (diag){
				console.log(diag)
			}else{
				console.log('diag canceled')
			}
			oobdpwa.readSingleFile(this.luaFileName)
			//this.nav2Main()
		},
		nav2Main() {
			router.push({ name: "Main" }); // always goes 'back enough' to Main
		},
		fileselect(ev){
			oobdpwa.readSingleFile(ev)
		}
	},
	mounted() {
		if (localStorage.connection) {
			this.connection = localStorage.connection;
		}
		if (localStorage.webSocketURL) {
			this.webSocketURL = localStorage.webSocketURL;
		}
	},
	watch: {
		connection(newConnection) {
			localStorage.connection = newConnection;
		},
		webSocketURL(newWebSocketURL) {
			localStorage.webSocketURL = newWebSocketURL;
		},
	},
};
</script>
