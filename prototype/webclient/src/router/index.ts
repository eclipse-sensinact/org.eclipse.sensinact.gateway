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

import Vue from 'vue'
import VueRouter, { RouteConfig } from 'vue-router'
import Map from "@/views/Map.vue";
import Tree from "@/components/Tree.vue";
import DatastreamsV from "@/views/Datastreams.vue";

Vue.use(VueRouter)

const routes: Array<RouteConfig> = [
  {
    path: '/details',
    name: 'map',
    component: Map,
    children:[{
      path: 'location/:id',
      component: Tree,
    }]
  },
  {
    path: '/',
    name: 'datastreams',
    component: DatastreamsV,
  }
]

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
})

export default router
