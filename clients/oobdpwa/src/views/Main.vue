<template>
	<v-card>
		<v-toolbar>
			<v-btn icon @click="backToMain()">
				<v-icon>mdi-skip-previous</v-icon>
			</v-btn>
			<v-btn icon>
				<v-icon>mdi-timer-outline</v-icon>
			</v-btn>
			<v-btn icon>
				<v-icon>mdi-refresh</v-icon>
			</v-btn>
			<v-spacer />
			{{ title }}
		</v-toolbar>
		<v-tabs grow>
			<v-tab>	Diagnose	</v-tab>
			<v-tab>	Output	</v-tab>
			<v-tab-item>
				<function-card
					v-for="item in items"
					:key="item.uuid"
					:item="item"
				/>
			</v-tab-item>
			<v-tab-item>
			</v-tab-item>
		</v-tabs>
	</v-card>
</template>
<script>
import router from "@/router";
import FunctionCard from "@/components/FunctionCard.vue";
export default {
	data() {
		return {
			title: "mein Titel",
			items:[
				{
					title: 'Joo',
					value: '15V',
					timer: false,
					refresh: true
				}
			]
		};
	},
	components:{
		FunctionCard
	},
	methods: {
		backToMain() {
			router.push({ name: "Start" });
		},
	},
};
</script>


