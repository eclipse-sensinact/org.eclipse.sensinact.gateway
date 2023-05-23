<!--
  Copyright (c) 2023 Contributors to the  Eclipse Foundation.

  This program and the accompanying materials are made
  available under the terms of the Eclipse Public License 2.0
  which is available at https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors: Markus Hochstein

-->

<template>


  <div class="grid" id="app">
    <div class="url colspan2 titlebar">
      <div class="logo">
        <div class="white triangle"></div>
        <div class="small logo smart_city_project" id="logo"></div>

      </div>
      <!--<b-field>
        <b-input v-model="baseurl"></b-input>
        <b-button  outlined @click="connect()">Connect</b-button>
      </b-field>-->
    </div>
    <div class="map_holder">
      <l-map id="map" :zoom="zoom" :center="center" @click="deselect">
        <l-tile-layer :url="url" :attribution="attribution"></l-tile-layer>
        <v-marker-cluster :options="{spiderfyDistanceMultiplier:3.2,animate:true,animateAddingMarkers:true}"
                          ref="clusterRef" v-if="points">
          <custom-marker @click.native="(ev)=>{ev.stopImmediatePropagation();markerWasClicked(point)}"
                          :marker="ret(point.location.coordinates)" v-for="point in points" :key="point['dsid']"
                          :lat-lng="res(point.location.coordinates)">
            <!--<l-icon class-name="custom-div-icon">-->

            <div class='marker-pin' :class="{'selected':point===selected}">
              <div class="round">
                <svg-icon type="mdi"
                          :size="24"
                          v-if="getPath([datastreamsbyID[point['dsid']].properties['sensorthings.datastream.type']])"
                          :path="getPath([datastreamsbyID[point['dsid']].properties['sensorthings.datastream.type']])"
                          class="marker_svg"></svg-icon>
                <div class="svg_icon dark"
                      :class="[datastreamsbyID[point['dsid']].properties['sensorthings.datastream.type']]"
                      v-else-if="datastreamsbyID[point['dsid']] && datastreamsbyID[point['dsid']].properties['sensorthings.datastream.type']">
                </div>
                <!--<div class="add" v-if="datastreamsbyID[point['dsid']].properties.originalName">
                  {{datastreamsbyID[point['dsid']].properties.originalName}}
                </div>-->
              </div>
            </div>
            <!--          <div class="marker-value">
                        <div class="span" v-if="obs[point['dsid']]">
                          <template v-if="obs[point['dsid']].result">
                          {{obs[point['dsid']].result}}
                          </template>
                        </div>
                        <div class="unit" v-if="datastreamsbyID[point['dsid']] && datastreamsbyID[point['dsid']].unitOfMeasurement">
                          {{datastreamsbyID[point['dsid']].unitOfMeasurement.name}}
                        </div>
                      </div>-->

            <div class="marker-value">
              <Datapoint :id="point['dsid']" :unit="datastreamsbyID[point['dsid']].unitOfMeasurement.name"></Datapoint>
              <!--<div class="span" v-if="point['data'] && point['data'].result">
                {{point['data'].result}}

              </div>-->
            </div>

            <!--</l-icon>-->

          </custom-marker>
        </v-marker-cluster>
      </l-map>
    </div>
    <div class="sidebar_holder absolute">
      <StreamTree ref="streamTree" @selection="select"></StreamTree>
    </div>
    <div class="propertie_holder absolute" v-if="selected!==null">
      <b-button class="absbtn" type="is-text" rounded size="is-small"
                icon-right="close" @click="deselect">
      </b-button>
      <PropertiesC :data="selectedData"></PropertiesC>
    </div>

  </div>
</template>

<script lang="ts">
import {Component, Vue, Watch} from "vue-property-decorator";
import {LIcon, LMap, LMarker, LTileLayer, LWMSTileLayer} from "vue2-leaflet";
import {
  LocationsApi,
  Location,
  Locations,
  Configuration,
  Datastream,
  ThingsApi,
  DatastreamsApi,
  ObservationsApi, Observations, Datastreams
} from "../../openapi/client";
import PropertiesC from "@/components/PropertiesView/Properties.vue";
import {BASE_PATH} from "../../openapi/client/base";
import {getBaseUrl, setBaseUrl} from "@/config/base";
import StreamTree from "@/components/StreamTree.vue";
import {AxiosResponse} from "axios";
//@ts-ignore
import Vue2LeafletMarkerCluster from 'vue2-leaflet-markercluster';
import Vue2LeafletMarkercluster from "vue2-leaflet-markercluster/Vue2LeafletMarkercluster.vue";
//@ts-ignore
import CustomMarker from 'vue-leaflet-custom-marker';
import Datapoint from "@/components/Datapoint.vue";
import { getPath } from "@/helper/SVGPaths";
//@ts-ignore
import SvgIcon from '@jamescoyle/vue-icon';

export interface LocationsPlus {
  dsid: String | undefined
}

@Component({
  components: {
    Datapoint,
    StreamTree,
    PropertiesC,
    LMap,
    LTileLayer,
    LMarker,
    LIcon,
    LWMSTileLayer,
    'v-marker-cluster': Vue2LeafletMarkercluster,
    CustomMarker,
    SvgIcon
  }
})
export default class DatastreamsV extends Vue {
  private url = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
  //private url = 'https://map.jena.de/wms/kartenportal';
  private attribution =
    '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors';
  private zoom = 15;

  private center = [50.93115286, 11.60392726];
  private markerLatLng = [55.8382551745062, -4.20119980206699]
  private points: Array<Location> | undefined = [];
  private selected: Location | null = null;
  private obs: any = {};
  private treeData: unknown = null;
  //private baseurl:string = 'https://sensors.bgs.ac.uk/FROST-Server';
  private baseurl: string = getBaseUrl();
  private datastreams: Datastreams | null = null;
  private datastreamsbyID: { [key: string]: Datastream } = {};
  private timer: any = null;
  private selectedData: any = null;

  async mounted() {
    await this.load();
  }

  async load() {
    ///@ts-ignore
    this.datastreams = (await new DatastreamsApi(new Configuration({basePath: getBaseUrl()}))
      .v11DatastreamsGet()).data as Datastreams
    (this.$refs.streamTree as StreamTree).getDatascreamsTree(this.datastreams)
    if (this.datastreams.value) {
      for (let datastream of this.datastreams.value) {
        //@ts-ignore
        this.datastreamsbyID[datastream["@iot.id"]] = datastream;
      }
    }

  }

  deselect() {
    this.selected = null;
    //@ts-ignore
    this.selectedData = null;
  }

  res(arr: any) {
    return [arr[1], arr[0]]
  }

  ret(arr: any) {
    return {
      lat: arr[1],
      lng: arr[0]
    }
  }

  connect() {
    setBaseUrl(this.baseurl)
    this.load();

  }

  markerWasClicked(point: Location & LocationsPlus) {
    this.selected = point;
    //@ts-ignore
    this.selectedData = {data: this.datastreamsbyID[point['dsid']], type: 'FMM_DATASTREAM'}
  }


  async select(model: Datastream[]) {
    this.points = undefined;
    let proms: Promise<any>[] = [];
    model.forEach((datastream: Datastream) => {
      if (datastream && datastream["@iot.id"]) {

        //@ts-ignore
        proms.push(new Promise(async (res, rej) => {
          try {
            //@ts-ignore
            let result = await new ThingsApi(new Configuration({basePath: getBaseUrl()})).v11ThingsEntityIdLocationsGet((datastream["@iot.id"].toString().split('~')[0]));
            if (result.data && result.data.value && result.data.value[0]) {
              //@ts-ignore
              (result.data.value[0] as LocationsPlus)['dsid'] = datastream["@iot.id"];
            }
            res(result);
          } catch (e) {
            rej(e)
          }
        }));
      }
    })
    let thingsLoaction: AxiosResponse<Locations & LocationsPlus>[] = await Promise.all(proms);

    this.points = thingsLoaction.map((e: AxiosResponse<Locations & LocationsPlus>) => {
      return (e.data.value![0])
    });
    this.$sstore.obs.setPoints(this.points);
    this.$sstore.obs.getDataForPoints();
    if (this.points.length > 0) {
      this.$sstore.obs.settimer();
    } else {
      this.$sstore.obs.clearTimer();
    }


  }

  beforeDestroy() {
    this.$sstore.obs.clearTimer();
  }
  getPath(id:string){
    return getPath(id[0])
  }
}
</script>

<style scoped lang="scss">
@import "@/scss/general.scss";
@import "~leaflet.markercluster/dist/MarkerCluster.css";
@import "~leaflet.markercluster/dist/MarkerCluster.Default.css";

#map {
  /*position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;*/
}

.grid {
  display: grid;
  grid-template-columns: 1fr;
  grid-template-rows: 55px 1fr;
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: #272727;
}

.rim {
  padding: 5px;
}

.sidebar_holder {
  bottom: 0;
  left: 0;
  width: 350px;
  top: 55px;
  z-index: 5;
  background: #3a3a3af7;
  box-shadow: 5px 6px 5px #0000003d;

  .plane {
    padding: 25px 0px 5px 0px;
    background: transparent;
  }
}

.marker-pin {
  width: 40px;
  height: 40px;
  border-radius: 50% 50% 50% 0;
  background: #002770;
  position: absolute;
  transform: rotate(-45deg);
  left: 50%;
  top: 50%;
  margin: -15px 0 0 -15px;
  box-shadow: -8px 15px 15px 0px rgb(0 0 0 / 43%)
}

.marker-pin.selected {
  .round {
    background: $bs-blue;
  }

  .svg_icon {
    &.dark {
      background: #fcfcfc;
    }
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

.custom-div-icon .marker-pin.selected i {
  color: #fff;
}

.colspan2 {
  grid-column: 1 / 3;
}

.marker-value {
  position: absolute;
  left: 12px;
  border: 1px solid #ccc;
  top: -10px;
  background: #fff;
  padding: 4px;
  border-radius: 21px;
  /* box-shadow: 3px 14px 15px 0px rgba(0, 0, 0, 0.43); */
  font-size: 16px;
}

.round {
  width: 34px;
  height: 34px;
  border-radius: 100%;
  transform: rotate(45deg);
  margin: 3px;
  background: #ffffff;
  display: flex;
  font-size: 21px;
  flex-direction: row;
  align-content: center;
  justify-content: center;
  align-items: center;
}

#logo {
  width: 237px;
  height: 97px;
  position: absolute;
  left: 3px;
  top: -20px;
  background-size: 138px;
  background-position: 0px;
}

#claim {
  width: 131px;
  height: 97px;
  position: absolute;
  left: 49px;
  top: -50px;
  background-size: 240px;
  background-position: -109px;
}

.logo {
  overflow: hidden;
  height: 56px;
  width: 214px;
  position: relative;
}

.titlebar {

  background: $bs-blue;
  height: 100%;
  display: flex;
  flex-direction: row;
  align-content: center;
  justify-content: flex-start;
  align-items: center;
  box-shadow: 0px 12px 14px #00000059;
  z-index: 7;
  position: relative;
}

.map_holder {
  z-index: 5;
  position: relative;
}

.propertie_holder {
  bottom: 0;
  left: 350px;
  /* width: 350px; */
  height: 351px;
  right: 0;
  z-index: 6;
  background: rgba(58, 58, 58, 0.968627451);
  box-shadow: 5px 6px 5px rgba(0, 0, 0, 0.2392156863);
}

.svg_icon {
  width: 24px;
  height: 24px;

  &.dark {
    background: #363636;
  }

}

.absbtn {
  position: absolute;
  right: 0;
  top: 5px;
  z-index: 1141;
}
.marker_svg{
  color:#333;
}
</style>
<style lang="scss">

#app {
  .marker-cluster {
    background-color: rgb(27 52 111 / 19%);

    div {
      background-color: rgb(27 52 111 / 100%);
      color: #fcfcfc;
    }
  }
}

</style>
