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
      <v-treeview v-model="treeData" :treeTypes="treeTypes" @selected="selected" :openAll="openAll" :contextItems="contextItems" @contextSelected="contextSelected"></v-treeview>


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
  Location,
  LocationsApi, Observation, Observations,
  Thing,
  Things,
  ThingsApi
} from "../../openapi/client";
import ThingsC from "@/components/Thing.vue";
//@ts-ignore
import VTreeview from "v-treeview"
import {getBaseUrl} from "@/config/base";
@Component({components:{
    ThingsC,
    VTreeview
  }})
export default class TreeC extends Vue{

  private loading = false;
  private location:Location|null = null;
  private openAll = true;
  private treeTypes = [
    {
      type: "#",
      max_children: 6,
      max_depth: 25,
      valid_children: [
        "FMM_THINGS",
        "FMM_LOC",
        "FMM_THING",
        "FMM_DATASTREAMS",
        "FMM_DATASTREAM",
        "FMM_OBSERVATION"
      ]
    },
    {
      type: "FMM_THINGS",
      icon: "fa-regular fa-circle",
      valid_children: ["Basic", "Top-up",'FMM_THING']
    },
    {
      type: "FMM_LOC",
      icon: "fa-regular fa-map",
      valid_children: ["Basic", "Top-up",'FMM_THINGS']
    },
    {
      type: "FMM_THING",
      icon: "fa-solid fa-circle",
      valid_children: ["Basic", "Top-up","FMM_DATASTREAMS"]
    },
    {
      type: "FMM_DATASTREAMS",
      icon: "fa-solid fa-rss",
      valid_children: ["Basic", "Top-up","FMM_DATASTREAM"]
    },
    {
      type: "FMM_DATASTREAM",
      icon: "fa-solid fa-rss",
      valid_children: ["Basic", "Top-up","FMM_OBSERVATION"]
    },
    {
      type: "FMM_OBSERVATION",
      icon: "far fa-user",
      valid_children: ["Basic", "Top-up"]
    },
    {
      type: "FMM_PARENT_IN_LAW",
      icon: "far fa-user",
      valid_children: ["Basic", "Top-up"]
    },
    {
      type: "Basic",
      icon: "far fa-hospital",
      valid_children: ["Top-up"]
    },
    {
      type: "Top-up",
      icon: "far fa-plus-square",
      valid_children: []
    }
  ];
  private treeData:any = [];
  private contextItems:any = [];
  private selectedNode:any = null

  mounted(){
    this.loadData()
  }
  async loadData(){
    try{
      this.treeData = [];
      this.loading = true;
      //@ts-ignore
      this.location = (await new LocationsApi(new Configuration({basePath:getBaseUrl()})).v11LocationsEntityIdGet(this.$route.params.id)).data;
      console.log(this.location)
      this.treeData.push({
        id: Math.random()*100000, text: "Location "+this.location.name, type: "FMM_LOC", count: 0,
        children: [
          {id: Math.random()*100000, text: "THINGS", type: "FMM_THINGS", children:[]}
        ]
      },)

    }catch (e){
      console.log(e);
      this.$router.push({name:'Map'})
    }
    finally{
      this.loading = false;
    }
  }
  @Watch('$route.params.id')
  id_changed(){
    this.loadData()
  }
  async selected(node:any){
    this.selectedNode = node;
    switch(node.model.type){
      case 'FMM_LOC':
        this.$emit('TreeSelect',{type:'FMM_LOC',data:this.location})
        break;
      case 'FMM_THINGS':
          this.$emit('TreeSelect',{type:'FMM_THINGS',data:null})
          node.model.children=[];
          (await this.getThingsTree()).forEach((child:unknown)=>{
            node.addNode(child)
          })
        break;
      case 'FMM_THING':{
        this.$emit('TreeSelect',{type:'FMM_THING',data: node.model._data})
        node.model.children=[]
        const datastreams: unknown[] = await this.getDatascreamsTree(node.model._data['@iot.id']);
        const datastreamsNode:any = {id: Math.random()*100000, text:'DATASTREAMS', type: "FMM_DATASTREAMS", children: []}
        datastreams.forEach((child:unknown)=>{
          datastreamsNode.children.push(child);
        })
        node.addNode(datastreamsNode)
        break;
      }
      case 'FMM_DATASTREAMS':
        this.$emit('TreeSelect',{type:'FMM_DATASTREAMS',data:null});
        break;
      case 'FMM_DATASTREAM':
        this.$emit('TreeSelect',{type:'FMM_DATASTREAM',data: node.model._data});
        /*node.model.children=[]
        const observations:Object[] = await this.getObservationTree(node.model._data['@iot.id'])
        node.children = [];
        observations.forEach((child:Object)=>{
          node.addNode(child)
        })*/
        break;
    }
  }
  async getThingsTree(): Promise<unknown[]>{
    this.loading = true;
    //@ts-ignore
    const things = (await new LocationsApi(new Configuration({basePath:getBaseUrl()})).v11LocationsEntityIdThingsGet(this.$route.params.id)).data as Things
    this.loading = false;
    let ret:unknown[] = [];
    things.value?.forEach((thing:Thing)=>{
      let node = {id: Math.random()*100000, text:  (thing as Thing).name, type: "FMM_THING", children: [],_data:thing}
      ret.push(node);
    });
    return ret;
  }

  async getDatascreamsTree(id:string): Promise<unknown[]>{
    this.loading = true;
    //@ts-ignore
    const datastreams = (await new ThingsApi(new Configuration({basePath:getBaseUrl()})).v11ThingsEntityIdDatastreamsGet(id)).data as Datastreams
    this.loading = false;
    let ret:unknown[] = [];
    datastreams.value?.forEach((datastream:Datastream)=>{
      let node = {id: Math.random()*100000, text:  (datastream as Datastream).name, type: "FMM_DATASTREAM", children: [],_data:datastream}
      ret.push(node);
    });
    return ret;
  }
  async getObservationTree(id:string): Promise<unknown[]>{
    this.loading = true;
    //@ts-ignore
    const observations = (await new DatastreamsApi(new Configuration({basePath:getBaseUrl()})).v11DatastreamsEntityIdObservationsGet(id)).data as Observations
    this.loading = false;
    let ret:unknown[] = [];
    observations.value?.forEach((observation:Observation)=>{
      let node = {id: Math.random()*100000, text:  (observation as Observation)["@iot.id"], type: "FMM_OBSERVATION", children: [],_data:observation}
      ret.push(node);
    });
    return ret;
  }

  getTypeRule(type:any) {
    var typeRule = this.treeTypes.filter(t => t.type == type)[0];
    return typeRule;
  }
  contextSelected(command:any) {
    switch (command) {
      case "Create Basic":
        this.selectedNode.addNode({
          text: "New Basic Plan",
          type: "Basic",
          children: []
        });
        break;
      case "Create Top-up":
        this.selectedNode.addNode({
          text: "New Top-up",
          type: "Top-up",
          children: []
        });
        break;
      case "Rename":
        this.selectedNode.editName();
        break;
      case "Remove":
        break;
    }
  }


}
</script>

<style scoped lang="scss">

.plane{
  height: 100%;
  z-index: 500;
  background: #3a3a3a;
  border-radius: 2px;
  overflow-y: auto;
  text-align: left;
  color: #d8d8d8;
  position: relative;
}
.cap{
  font-style: italic;
  font-weight: bold;

}
.pad{
  padding: 0 10px;
}
</style>
<style lang="scss">
@import "./../scss/general";
ul .tree-node{
  white-space: nowrap;
}
ul .tree-node :hover::before {
  background: rgba(135, 147, 161, 0.27) !important;
}
ul .tree-node input[type=radio]:checked+label:before{
  background: #4099ff3d !important;
}

</style>
