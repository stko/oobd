<template>
	<v-app toolbar footer dark>
		<!-- Provides the application the proper gutter -->
		<v-main>
			<v-container>
				<router-view/>
			</v-container>
		</v-main>
		<v-footer app dark>
		</v-footer>
	</v-app>
</template>

<script>

export default {
  name: 'App',

  components: {
  },

  data: () => ({
    //
  }),
};
</script>
