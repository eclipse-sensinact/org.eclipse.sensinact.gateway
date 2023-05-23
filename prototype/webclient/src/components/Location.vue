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
        <b-tabs v-model="activeTab" class="nopad">
          <b-tab-item label="Location">

            <div class="dtable">
              <perfect-scrollbar>
                <div class="cap">{{this.$i18n.t('description')}}: </div>
                <div>{{data.description}}</div>
                <div class="cap">{{this.$i18n.t('encodingType')}}: </div>
                <div>{{data.encodingType}}</div>

                <div class="cap">{{this.$i18n.t('name')}}: </div>
                <div>{{data.name}}</div>


                <div class="cap">{{this.$i18n.t('location')}}: </div>
                <div v-if="data.location">[{{data.location.coordinates.join(';')}}]</div>
              </perfect-scrollbar>
            </div>

          </b-tab-item>

          <b-tab-item :label="this.$i18n.t('properties').toString()">

            <div class="dtable">
              <perfect-scrollbar>
              <div class="item" v-for="(value,key) in data.properties" :key="key">
                <div class="key cap">{{key}}:</div>
                <div class="value">{{value}}</div>
              </div>
            </perfect-scrollbar>
            </div>

          </b-tab-item>

        </b-tabs>


      </div>
  </div>
</template>

<script lang="ts">

import {Component, Prop, Vue} from "vue-property-decorator";
import ThingsC from "@/components/Thing.vue";
@Component({components:{
    ThingsC
  }})
export default class LocationC extends Vue{

  private loading = false;
  private activeTab = 0;
  @Prop() readonly data:unknown;

}
</script>

<style scoped lang="scss">
.plane{

  overflow-y: auto;
}
.cap{
  font-style: italic;
  font-weight: bold;

}
.pad{
  padding: 0 10px;
}

</style>
