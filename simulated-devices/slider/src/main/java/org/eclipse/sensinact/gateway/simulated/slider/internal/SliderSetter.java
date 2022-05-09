/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.slider.internal;

import org.eclipse.sensinact.gateway.simulated.slider.api.SliderSetterItf;

public class SliderSetter implements SliderSetterItf {

    private final SliderAdapter listener;

    public SliderSetter(SliderAdapter listener) {
        this.listener = listener;
    }

    public void move(int value) {
        this.listener.mouseReleased(value);
    }

    public void stop() {
    }
}
