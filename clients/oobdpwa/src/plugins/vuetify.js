import Vue from 'vue';
import Vuetify from 'vuetify/lib/framework';
// import 'vuetify/src/stylus/app.styl'
import manifestJSON from '../../public/manifest.json'

import {
//  Vuetify,
  VApp,
  VBtn,
  VBtnToggle,
  VCard,
  VCheckbox,
  VDivider,
  VGrid,
  VIcon,
  VList,
  VProgressLinear,
  VTextField
} from 'vuetify'

Vue.use(Vuetify, {
    components: {
      VApp,
      VBtn,
      VBtnToggle,
      VCard,
      VCheckbox,
      VDivider,
      VGrid,
      VIcon,
      VList,
      VProgressLinear,
      VTextField
    },
    theme: {
      primary: manifestJSON.theme_color
    }
  });

export default new Vuetify({
});
