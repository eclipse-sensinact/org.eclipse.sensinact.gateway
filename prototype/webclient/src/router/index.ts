/*********************************************************************
 * Copyright (c) YYYY Contributors to the Eclipse Foundation.
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

Vue.use(VueRouter)

const routes: Array<RouteConfig> = [
  {
    path: '/',
    name: 'map',
    component: Map,
    children:[{
      path: 'location/:id',
      component: Tree,
    }]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
