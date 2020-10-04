/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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
