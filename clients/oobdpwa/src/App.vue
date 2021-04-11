<template>
	<v-app toolbar footer dark>
		<!-- Provides the application the proper gutter -->
		<v-main>
			<v-container>
				<router-view />
			</v-container>
		</v-main>
		<v-dialog dark v-model="text_dialog.show" scrollable max-width="300px">
			<v-card>
				<v-card-title>{{ text_dialog.title }}</v-card-title>
				<v-divider></v-divider>
				<v-card-text>
							<v-text-field
		label="Your Input"
		v-model="text_dialog.value">

		</v-text-field>
				</v-card-text>
				<v-divider></v-divider>
				<v-card-actions>
					<v-btn text @click="text_dialog_ok(false)">Cancel</v-btn>
					<v-btn text @click="text_dialog_ok(true)">OK</v-btn>
				</v-card-actions>
			</v-card>
		</v-dialog>
		<v-footer app dark> </v-footer>
	</v-app>
</template>

<script>
export default {
	name: "App",

	components: {},
	data () {
			return {
			//
				'text_dialog':{
					show: false,
					resolve:null,
					reject:null,
					value:''
				}
			}
	},
	methods: {
		text_dialog_ok(ok) {
			if (ok){
				this.text_dialog.resolve(this.text_dialog.value)
			}else{
				this.text_dialog.reject()
			}
			this.text_dialog.show=false
		},
		async show_text_dialog(title, value) {
			var thisVue = this
			try{
				const answer = await new Promise(function(resolve, reject){
					thisVue.text_dialog.resolve=resolve
					thisVue.text_dialog.reject=reject
					thisVue.text_dialog.title=title
					thisVue.text_dialog.value=value
					thisVue.text_dialog.show=true
				})
				return answer

			} catch (error) {
				return null
			}
		},
	},
	provide: function () {
		return {
			text_dialog:  this.text_dialog,
			show_text_dialog:  this.show_text_dialog
		}
	}
};
</script>
