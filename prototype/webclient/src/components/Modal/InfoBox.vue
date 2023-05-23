<!--
Copyright (c) 2023 Contributors to the  Eclipse Foundation.
This program and the accompanying materials are made
available under the terms of the Eclipse Public License 2.0
which is available at https://www.eclipse.org/legal/epl-2.0/
SPDX-License-Identifier: EPL-2.0

Contributors: Smart City Jena

-->
<template>

  <b-modal
    v-model="active"
    has-modal-card
    trap-focus
    :destroy-on-hide="true"
    aria-role="dialog"
    aria-label="Example Modal"
    close-button-aria-label="Close"
    aria-modal>


    <div class="card" style="width: auto">
      <div class="card-content">
        <iframe v-if="infoUri" :src="infoUri" name="InfoIFrame" title="infobox" id="infoFrame"></iframe>
      </div>
    </div>

  </b-modal>


</template>
<script lang="ts">

import {Vue, Component, Prop} from "vue-property-decorator";

@Component
export default class InfoBox extends Vue {
  @Prop({default: () => ''}) readonly infoUri: string | undefined;
  private active = true;

}
</script>

<style lang="scss" scoped>
#infoFrame {
  width: 800px;
  height: 600px;
}
</style>
