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



let config= {
  baseUrl:window.location.origin+'/sensinact/rest',
  //baseUrl: 'https://udp-5g-broker.nomad-dmz.jena.de/sensinact/rest'
};

export function setBaseUrl(url:string){
  config.baseUrl = url;
}
export function getBaseUrl(){
  return config.baseUrl;
}
