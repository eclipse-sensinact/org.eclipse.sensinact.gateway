import Vue from 'vue'
import VueRouter, { RouteConfig } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import Map from "@/views/Map.vue";
import LocationC from "@/components/Location.vue";
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
