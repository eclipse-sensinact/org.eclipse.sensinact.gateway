<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors: Markus Hochstein
-->

<template>
  <div id="app">
    <router-view/>
    <InfoBox v-if="showInfoBox" :infoUri="infoUri||''"></InfoBox>
  </div>

</template>

<style lang="scss">
#app {
  /*font-family: Avenir, Helvetica, Arial, sans-serif;*/
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

nav {
  padding: 30px;

  a {
    font-weight: bold;
    color: #2c3e50;

    &.router-link-exact-active {
      color: #42b983;
    }
  }
}
</style>

<script lang="ts">

import {Vue} from "vue-property-decorator";
import axios from "axios";
import Component from "vue-class-component";
import InfoBox from "@/components/Modal/InfoBox.vue";

@Component({
  components:{
    InfoBox
  }
})
export default class App extends Vue {

  private showInfoBox = false;
  private infoUri = null;

  async mounted() {
    try {
      const base = window.location.protocol + '//' + window.location.host;
      const config = (await axios.get(`config/config.json`)).data;
      if (config && config.INFO_CHECK_URI && config.INFO_CHECK_URI) {
        this.infoUri = config.INFO_BASE_URI;
        this.showInfoBox = (await axios.get(config.INFO_CHECK_URI)).status == 200
      }
    } catch (e) {
      console.log(e)
    }
  }
}
</script>
