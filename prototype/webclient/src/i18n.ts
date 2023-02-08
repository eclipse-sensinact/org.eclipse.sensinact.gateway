/*********************************************************************
 * Copyright (c) YYYY Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Markus Hochstein
 **********************************************************************/

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
