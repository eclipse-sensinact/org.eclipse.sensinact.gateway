<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors: Markus Hochstein
-->

<template>
  <div class="t1">
    <b-loading :active="loading" :can-cancel="false" :is-full-page="false"></b-loading>
    <div v-if="data">

      <b-tabs v-model="activeTab" class="nopad" id="light">
        <b-tab-item label="Datastream">
          <div class="dtable" v-if="data">
            <perfect-scrollbar>
            <div class="item" v-for="(value,key) in noprops"  :key="key">
              <div class="key cap">{{key}}:</div>
              <div class="value">{{value}}</div>
            </div>
            </perfect-scrollbar>


          </div>
        </b-tab-item>

        <b-tab-item label="Eigenschaften">
          <div class="dtable" v-if="data.properties">
            <perfect-scrollbar>
            <div class="item" v-for="(value,key) in data.properties" :key="key">
              <div class="key cap">{{key}}:</div>
              <div class="value">{{value}}</div>
            </div>
            </perfect-scrollbar>


          </div>

        </b-tab-item>
        <b-tab-item label="Observations">
            <Observations v-if="activeTab===2" :id="data['@iot.id']" :title="data.name"></Observations>
        </b-tab-item>
      </b-tabs>

    </div>
  </div>
</template>

<script lang="ts">

import {Component, Prop, Vue, Watch} from "vue-property-decorator";
import {Datastream} from "../../openapi/client";
import Observations from "@/components/Observations.vue";
@Component({
  components: {Observations}
})
export default class DatastreamsC extends Vue{
  private loading = false;
  private activeTab = 0;
  @Prop() readonly data: Datastream | undefined;

  @Watch('data')
  dataChanged(newD:unknown){
    console.log(newD)
  }
  get noprops(){
    let ret:any = {};
    for (const [key,atr] of Object.entries(this.data as object)){
      if(key!=='properties')
        ret[key]=atr;
    }
    return ret;
  }

}
</script>

<style scoped lang="scss">
.plane{
  position: absolute;
  right: 15px;
  top: 15px;
  bottom: 15px;
  width: 350px;
  z-index: 500;
  background: #ffffffc7;
  border-radius: 24px;
  box-shadow: 0px 0px 12px -6px #0000008c;
}
.cap{
  font-style: italic;
  font-weight: bold;

}


</style>
<style lang="scss">
#light{
    .tabs ul{
      border-bottom-color: hsl(0deg 0% 35.43%);
    }
    nav a {
      font-weight: 200;
      color: #8b8b8b;
      border-bottom-color: hsl(0deg 0% 35.43%);
    }
    nav .is-active a{
      border-bottom-color: hsl(0, 1%, 70%);
      span{
        color: #fcfcfc!important;
      }
    }




}

</style>
