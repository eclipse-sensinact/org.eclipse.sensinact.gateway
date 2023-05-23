<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors:  Markus Hochstein
-->

<template>
  <div class="plane is-vertical" v-if="data && data.data">
    <Location v-if="data.type === 'FMM_LOC'" :data="data.data"></Location>
    <Thing v-if="data.type === 'FMM_THING'" :data="data.data"></Thing>
    <Datastreams v-if="data.type === 'FMM_DATASTREAM'" :data="data.data"></Datastreams>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue, Watch} from "vue-property-decorator";
import Location from "@/components/Location.vue";
import Thing from "@/components/Thing.vue";
import Datastreams from "@/components/Datastreams.vue";

@Component({
  components: {Datastreams, Location, Thing}
})
export default class PropertiesC extends Vue {
  @Prop() readonly data: any;

  @Watch('data') data_changed(new_data: any) {
    console.log('new data')
    console.log(new_data)
  }

}
</script>

<style scoped lang="scss">
.plane {
  height: 100%;
  width: 100%;
  z-index: 500;
  background: transparent;
  border-radius: 2px;
  overflow-y: auto;
  text-align: left;
  color: #d8d8d8;
  position: relative;
}

.tabs a {
  font-weight: 300;
}
</style>
