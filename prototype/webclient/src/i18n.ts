import VueI18n from "vue-i18n";
import TranslationDE from "@/locales/de/translation.json";

import Vue from "vue";

Vue.use(VueI18n);
const i18n:VueI18n = new VueI18n({
    locale: 'de', // set locale
    messages:{
        de:TranslationDE,
    },

});
export default i18n;
