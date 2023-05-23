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


import {Datastream, Datastreams} from "../../openapi/client";

export default function groupByName(datastreams:Datastreams){
  let ret:any = {}

  datastreams.value?.forEach((datastream:Datastream)=>{
    if(datastream.name) {
      if (!ret[datastream.name]) {
        ret[datastream.name] = [];
      }
      ret[datastream.name].push(datastream);
    }
  })
  return ret;
}

export function groupByCategory(datastreams:Datastreams){
  let ret:any = {}

  datastreams.value?.forEach((datastream:Datastream)=>{
    //@ts-ignore
    let type = datastream.properties['sensorthings.datastream.type'];
    if(datastream.properties && type && datastream.name) {
      if (!ret[type]) {
        ret[type] = [];
      }
      ret[type].push(datastream);
    }
  })
  return ret;
}
