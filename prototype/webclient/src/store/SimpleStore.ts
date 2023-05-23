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



/*export interface simpleStoreIF{
    toolname: ToolnameStore,
    toolImage: ToolImageStore,
    strokes: StokesStore,
    counters: CounterStore,
    headImage: HeadImageStore
}*/
import ObsStore from "@/store/ObsStore";

export interface simpleStoreIF{
    [index: string]: SimpleStore;
}



export const simpleStore:simpleStoreIF = {
    obs:new ObsStore(),

};

export default {
    install(Vue:any, options:any) {
        Vue.prototype.$sstore = simpleStore;
    },
    update(){
      for (let key in simpleStore){
          simpleStore[key].update();
      }
    }

};


export interface SimpleStore {
    state: any

    update():any

}
