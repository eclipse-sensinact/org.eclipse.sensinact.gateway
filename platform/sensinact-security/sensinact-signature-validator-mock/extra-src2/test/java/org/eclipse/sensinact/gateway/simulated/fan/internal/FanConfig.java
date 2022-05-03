/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.fan.internal;

import java.util.ArrayList;
import java.util.List;

public class FanConfig {
    private List<FanConfigListener> listeners;
    private int speed;
    private boolean on = false;

    public FanConfig() {
        this.speed = 10;
        this.listeners = new ArrayList<FanConfigListener>();
    }

    public void addListener(FanConfigListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FanConfigListener listener) {
        listeners.remove(listener);
    }

    private void updateSpeed(int speed) {
        for (FanConfigListener listener : listeners) {
            listener.speedChanged(speed);
        }
    }

    public void turnOn() {
        this.on = true;
        this.updateSpeed(this.speed);
    }

    public void turnOff() {
        this.on = false;
        this.updateSpeed(0);
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        if (on) {
            this.updateSpeed(this.speed);
        }
    }
}
