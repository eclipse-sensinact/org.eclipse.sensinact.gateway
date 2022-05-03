/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.light.internal;

import java.util.ArrayList;
import java.util.List;

public class LightConfig {
    private List<LightConfigListener> listeners;
    private int brightness;
    private boolean on = false;

    public LightConfig() {
        this.brightness = 10;
        this.listeners = new ArrayList<LightConfigListener>();
    }

    public void addListener(LightConfigListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LightConfigListener listener) {
        listeners.remove(listener);
    }

    private void updateBrightness(int brightness) {
        for (LightConfigListener listener : listeners) {
            listener.brightnessChanged(brightness);
        }
    }

    public void turnOn() {
        this.on = true;
        this.updateBrightness(this.brightness);
    }

    public void turnOff() {
        this.on = false;
        this.updateBrightness(0);
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
        if (this.on) {
            this.updateBrightness(brightness);
        }
    }
}
