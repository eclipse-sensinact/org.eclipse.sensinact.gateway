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
    <div class="chart">
      <Bar  :data="chartdata"  :options="chartOptions" style="{width: 100%;height: 250px}" css-classes="chart" responsive/>
    </div>
  </div>
</template>
responsive
<script lang="ts">
import 'chartjs-adapter-moment';
import {Component, Prop, Vue, Watch} from "vue-property-decorator";
import {Configuration, DatastreamsApi, Observations} from "../../openapi/client";

import { Bar } from 'vue-chartjs'
import {Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, TimeScale} from 'chart.js'
import moment from "moment";
import {getBaseUrl} from "@/config/base";

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale,TimeScale)

@Component({components:{Bar}})
export default class ObservationsC extends Vue{
  private loading = false;
  @Prop() readonly id: string | undefined;
  @Prop({default:()=>''}) readonly title: string | undefined ;
  private observations:Observations|null = null;
  private chartOptions:any ={
    maxBarThickness: 2, // number (pixels)
    barThickness: 'flex',
    plugins:{
      legend: {
        display: true
      }
    },
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        'x': {
          type: 'time',
          time: {
            displayFormats: {
              'millisecond': 'MMM DD',
              'second': 'MMM DD',
              'minute': 'MMM DD',
              'hour': 'MMM DD',
              'day': 'MMM DD',
              'week': 'MMM DD',
              'month': 'MMM DD',
              'quarter': 'MMM DD',
              'year': 'MMM DD',
            }
          },
          /*ticks: {
            // For a category axis, the val is the index so the lookup via getLabelForValue is needed
            callback: function (val: any, index: any): any {
              // Hide every 2nd tick label
              //@ts-ignore
              return index % 2 === 0 ? this.getLabelForValue(val) : '';
            }
          }*/
        }
      }
  }
  @Watch('id')
  dataChanged(newD:any){
    this.loadData()
  }
  mounted(){
    this.loadData()
  }
  async loadData(){
    this.loading = true;
    try{
      //@ts-ignore
      this.observations = (await new DatastreamsApi(new Configuration({basePath:getBaseUrl()})).v11DatastreamsEntityIdObservationsGet(this.id!)).data;
      console.log(this.observations)
    }catch (e){
      console.log(e);
    }finally {
      this.loading = false;
    }
  }
  get chartdata(){
    if(this.observations){
      return {
        labels: this.observations.value?.map(e=>moment(e.resultTime,"YYYY-MM-DD'T'HH:mm:ss.SSSZZ")),
        datasets: [
          {
            label: this.title,
            backgroundColor: 'rgb(36,97,162)',
            data: this.observations.value?.map(e=>e.result)
          }
        ]
      }
    }else{
      return {
        labels: [],
        datasets: [
          {
            label: '',
            backgroundColor: 'rgba(36,97,162,0.81)',
            data:  [],
          }
        ]
      }
    }
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
.chart{
  height:250px;
  width:100%;
}

</style>
