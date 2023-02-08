<!--
  Copyright (c) YYYY Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors:
     Markus Hochstein
-->

<template>
  <div class="grid">
    <div class="map_holder rim">
        <l-map id="map"  :zoom="zoom" :center="center">
          <l-tile-layer :url="url" :attribution="attribution"></l-tile-layer>
          <l-marker  v-for="point in points" :key="point['@iot.id']" :lat-lng="res(point.location.coordinates)"  @click="markerWasClicked(point)">
            <l-icon

                class-name="custom-div-icon"
               ><div class='marker-pin' :class="{'selected':point['@iot.id']==selected}">

                  <i class='mdi mdi-radio-tower'></i>
            </div>
            </l-icon>

          </l-marker>

        </l-map>
    </div>
    <div class="sidebar_holder rim">
         <router-view id="sidebar" @TreeSelect="(data)=>treeData=data"></router-view>
    </div>
    <div class="propertie_holder rim">
      <PropertiesC :data="treeData"></PropertiesC>
    </div>
    <div class="corner"> </div>
  </div>
</template>

<script lang="ts">
import {Component, Vue, Watch} from "vue-property-decorator";
import {LIcon, LMap, LMarker, LTileLayer} from "vue2-leaflet";
import {LocationsApi, Location, Locations} from "../../openapi/client";
import PropertiesC from "@/components/PropertiesView/Properties.vue";

@Component({components:{
    PropertiesC,
    LMap,
    LTileLayer,
    LMarker,
    LIcon
  }})
export default class Map extends Vue{
  private url:string =  'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
  private attribution:string =
      '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors';
  private zoom:number = 15;
  private center = [55.8382551745062, -4.20119980206699];
  private markerLatLng =[55.8382551745062, -4.20119980206699]
  private points:Array<Location>|undefined = [];
  private selected:Location|null = {'@iot.id':undefined};
  private treeData:any|null = null;

  async mounted(){
    await this.load();
  }
  async load(){
    this.points = ((await new LocationsApi().v11LocationsGet()).data as Locations)!.value;
  }

  res(arr:any){
    return [arr[1],arr[0]]
  }
  markerWasClicked(point:Location){

    this.$router.push('/location/'+point["@iot.id"])
    //this.$route.params
  }
  @Watch('$route.params',{immediate:true})
  params_chanded(params:any){
    this.selected = params.id;
  }


}
</script>

<style scoped lang="scss">
 @import "@/scss/general.scss";
  #map{
    /*position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;*/
  }
  .grid{
    display: grid;
    grid-template-columns: 1fr 350px;
    grid-template-rows: 1fr 350px;
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
    background: #272727;
  }
  .rim{
    padding: 5px;
  }
  .sidebar_holder{
    grid-row: span 2;
  }
  .marker-pin {
    width: 30px;
    height: 30px;
    border-radius: 50% 50% 50% 0;
    background: #fffcfc;
    position: absolute;
    transform: rotate(-45deg);
    left: 50%;
    top: 50%;
    margin: -15px 0 0 -15px;
    box-shadow: -8px 15px 15px 0px rgb(0 0 0 / 10%)
   }
 .marker-pin.selected{
   background:$primary;
   i{
     color: #fff;
   }
 }
  // to draw white circle
  .marker-pin::after {
    content: "";
    width: 24px;
    height: 24px;
    margin: 3px 0 0 -12px;
    //background: #fff;
    position: absolute;
    border-radius: 50%;
    transform: rotate(-45deg);
    //box-shadow: inset 0px 0px 3px 0px #00000078;
  }

  // to align icon
  .custom-div-icon i {
    position: absolute;
    width: 22px;
    font-size: 22px;
    left: 0;
    right: 0;
    margin: -1px 3px;
    text-align: center;
    color: $primary;
    transform: rotate(45deg);
  }
 .custom-div-icon .marker-pin.selected i{
   color: #fff;
 }


</style>
