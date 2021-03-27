<template>
	<!-- Grid card layout für die gefundenen Einträge? Kuckst Du hier https://codepen.io/munieru_jp/pen/jpdJNV-->

	<v-card>
		<v-toolbar color="yellow">
			<v-toolbar-items>
				<v-btn icon @click="edit_delete_dialog_show = true">
					<v-icon>mdi-delete</v-icon>
				</v-btn>
				<v-divider vertical></v-divider>
				<v-spacer></v-spacer>
			</v-toolbar-items>
		</v-toolbar>
		{{ $t("edit_select_header") }}

	</v-card>
</template>

<script>
import router from "../router";


export default {
	name: "Log",
	data: () => ({
	}),
	created() {
	},
	methods: {
		nav2Main() {
			router.push({ name: "Main" }); // always goes 'back enough' to Main
		},
	},
};
</script>

<style scoped>
h1,
h2 {
	font-weight: normal;
}

ul {
	list-style-type: none;
	padding: 0;
}

li {
	display: inline-block;
	margin: 0 10px;
}

a {
	color: #42b983;
}
</style>
