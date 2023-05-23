/*********************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import {SimpleStore} from "@/store/SimpleStore";
import Vue from "vue";
import {AxiosResponse} from "axios/index";
import {Configuration, Observations, ObservationsApi} from "../../openapi/client";
import {getBaseUrl} from "@/config/base";
import {LocationsPlus} from "@/views/Datastreams.vue";



export default class ObsStore implements SimpleStore {

  public state = Vue.observable({
    obs: {},

  });
  public loading:boolean = Vue.observable(false);
  private timer:any = null;
  private points:any = [];


  constructor(){


  };
  setPoints(points:any){
    this.points = points
  }
  async getDataForPoints(){
    let proms:Promise<AxiosResponse<Observations&LocationsPlus>>[] = []
    this.points?.forEach((point:any)=>{

      //@ts-ignore
      proms.push(
        new Promise(async (res,rej)=>{
          try{
            //@ts-ignore
            let result:AxiosResponse<> = await new ObservationsApi(new Configuration({basePath:getBaseUrl()})).v11ObservationsEntityIdDatastreamObservationsGet(point["dsid"]);
            if(result.data && result.data.value && result.data.value[0]){
              //@ts-ignore
              (result.data.value[0] as LocationsPlus)['dsid'] = point["dsid"];
            }
            res(result);
          }catch (e){
            rej(e)
          }
        }));

    })
    let promsSettled = await Promise.allSettled(proms);
    //this.obs= new Map();
    promsSettled.forEach((obj:any) => {
      if(obj.value && obj.value.data && obj.value.data.value && obj.value.data.value[0]){
        let value:String = obj.value.data.value[0]['dsid'] as String
        //@ts-ignore
        this.state.obs[value] = obj.value.data.value[0];
      }


    });
    this.state.obs = {...this.state.obs};
    //this.points = this.points?.slice(0);
    //console.log(this.points)


  }

  public settimer(){
    if(!this.timer){
      this.timer = setInterval(()=>this.getDataForPoints(),10000)
    }
  }
  public clearTimer(){
    if(this.timer) {
      clearInterval(this.timer)
      this.timer = null;
    }
  }
  update():any {

  }
}
