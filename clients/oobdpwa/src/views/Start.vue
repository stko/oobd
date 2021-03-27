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
		<v-file-input label="Select your Lua Script" v-model="luaFileName" required>
		</v-file-input>
		<v-btn block @click="start" :disabled="luaFileName.length == 0">
			Start
		</v-btn>
	</v-container>
</template>
<script>
export default {
	name: "Start",
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
		start() {
			console.log("start");
			console.log(this.luaFileName);
		},
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
