import Vue from 'vue'
import App from './App.vue'
import router from './router'
import 'leaflet/dist/leaflet.css';
import {Icon} from "leaflet";
import Buefy from 'buefy'
import 'buefy/dist/buefy.css'
import i18n from "@/i18n";
import VueI18n from "vue-i18n";
// @ts-ignore
import VueTreeList from 'vue-tree-list';
import './scss/general.scss';
import '@mdi/font/css/materialdesignicons.css'
import PerfectScrollbar from "vue2-perfect-scrollbar";
import "vue2-perfect-scrollbar/dist/vue2-perfect-scrollbar.css";

Vue.use(PerfectScrollbar);
Vue.use(VueTreeList)

Vue.config.productionTip = false
//@ts-ignore
delete Icon.Default.prototype._getIconUrl;
Icon.Default.mergeOptions({
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

type D = Icon.Default & {
  _getIconUrl?: string;
};

delete (Icon.Default.prototype as D)._getIconUrl;
Vue.use(Buefy);
Vue.use(VueI18n);

new Vue({
  router,
  i18n,
  render: h => h(App)
}).$mount('#app')
