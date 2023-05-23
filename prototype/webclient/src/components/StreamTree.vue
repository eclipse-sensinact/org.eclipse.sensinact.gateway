<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors: Markus Hochstein
-->


<template>
  <div class="plane tree">
    <b-loading :active="loading" :can-cancel="false" :is-full-page="false"></b-loading>
    <perfect-scrollbar>
      <div class="tree">
        <div class="leaf" v-for="leaf in treeData" @click="selected(leaf)" :class="[{'active': leaf.active},leaf.text]"
              :key="leaf.key">
          <div class="cat_icon">
            <svg-icon type="mdi" v-if="getPath([leaf.text])" :path="getPath([leaf.text])" :size="35" class="svg_icon2"></svg-icon>
            <div class="svg_icon" v-else :class="[leaf.text]"></div>
            <b-tag rounded type="is-primary">{{ leaf._data.length }}</b-tag>
          </div>

          {{ $t('prop.' + leaf.text) }}

        </div>
      </div>

    </perfect-scrollbar>
  </div>
</template>

<script lang="ts">

import {Component, Vue, Watch} from "vue-property-decorator";
import {
  Configuration,
  Datastream,
  Datastreams,
  DatastreamsApi,
  Location, Locations,
  LocationsApi, Observation, Observations,
  Thing,
  Things,
  ThingsApi
} from "../../openapi/client";
import ThingsC from "@/components/Thing.vue";
//@ts-ignore
import VTreeview from "v-treeview"
import {getBaseUrl} from "@/config/base";
import groupByName, {groupByCategory} from "@/helper/DatastreamGroups";
//@ts-ignore
import SvgIcon from '@jamescoyle/vue-icon';

import { getPath } from "@/helper/SVGPaths";

@Component({
  components: {
    VTreeview,
    SvgIcon
  }
})
export default class StreamTreeC extends Vue {

  private loading = false;


  private treeData: any = [];

  private selectedNodesKeys: any = {};

  @Watch('$route.query') query_changed(new_query_params: any) {
    if (new_query_params.enabledCategories) {
      let array_of_key_to_select = new_query_params.enabledCategories.split(',');
      this.treeData.forEach((node: any) => {
        if (array_of_key_to_select.includes(node.key)) {
          this.selectedNodesKeys[node.key] = node._data;
          node.active = true;
        }
      })
      let emit: any = [];
      for (let key in this.selectedNodesKeys) {
        console.log(this.selectedNodesKeys[key])
        emit = emit.concat(this.selectedNodesKeys[key])
      }
      console.log(emit)
      //console.log(Object.keys(this.selectedNodesKeys).join(','))
      //this.$router.replace({ name: 'datastreams', query: { enabledCategories: Object.keys(this.selectedNodesKeys).join(',') }})
      this.$emit('selection', emit)
    }
  }

  mounted() {
    //this.getDatascreamsTree()
    console.log(this.$route.query)
  }

  getPath(id:string){
    return getPath(id[0])
  }

  async selected(node: any) {
    console.log(this.selectedNodesKeys)
    if (node.active) {
      delete this.selectedNodesKeys[node.key];
      node.active = false;
    } else {
      this.selectedNodesKeys[node.key] = node._data;
      node.active = true;
    }

    /*let emit:any = [];
    for(let key in this.selectedNodesKeys){
      console.log(this.selectedNodesKeys[key])
      emit = emit.concat(this.selectedNodesKeys[key])
    }
    console.log(emit)
    console.log(Object.keys(this.selectedNodesKeys).join(','))*/
    this.$router.replace({
      name: 'datastreams',
      query: {enabledCategories: Object.keys(this.selectedNodesKeys).join(',')}
    })
    //this.$emit('selection', emit)

  }


  async getDatascreamsTree(datastreams: Datastreams) {
    this.loading = true;

    this.loading = false;
    let ret: unknown[] = [];
    let groups = groupByCategory(datastreams)
    for (const [key, value] of Object.entries(groups)) {


      let node = {
        id: Math.random() * 100000,
        text: key,
        type: "FMM_DATASTREAM",
        children: [],
        _data: value,
        key: key,
        active: false
      }
      ret.push(node);
    }
    this.treeData = ret;
    this.query_changed(this.$route.query);
  }

  get isactive() {
    console.log(this.selectedNodesKeys);
    return Object.keys(this.selectedNodesKeys)
  }

  set isactive(val: any) {

  }
}


</script>

<style scoped lang="scss">

.plane {
  height: 100%;
  z-index: 500;
  background: #3a3a3a;
  border-radius: 2px;
  overflow-y: auto;
  text-align: left;
  color: #d8d8d8;
  position: relative;
}

.cap {
  font-style: italic;
  font-weight: bold;

}

.pad {
  padding: 0 10px;
}

.leaf {
  padding-left: 15px;
  display: grid;
  grid-template-columns: 50px 1fr;
  grid-template-rows: 45px;
  align-items: center;
  cursor: pointer;

  &.active {
    //background: #1b346fc7;
    background: rgb(27, 52, 111);
    background: linear-gradient(90deg, rgba(27, 52, 111, 0.78) 0%, rgba(27, 52, 111, 0) 100%);
  }
}

.cat_icon {
  width: 40px;
  height: 40px;
  position: relative;

  border-radius: 4px;
  border: 1px solid #999;

  .mdi {
    font-size: 25px;
  }

  .tag {
    position: absolute;
    right: -7px;
    bottom: -6px;
    color: #bbb;
    background: transparent;
  }

  .svg_icon {

    height: 24px;
    width: 24px;
    margin: 7px 2px;

  }
}
</style>
<style lang="scss">
@import "./../scss/general";

ul .tree-node {
  white-space: nowrap;
}

ul .tree-node :hover::before {
  background: rgba(135, 147, 161, 0.27) !important;
}

ul .tree-node input[type=radio]:checked + label:before {
  background: #4099ff3d !important;
}


</style>
