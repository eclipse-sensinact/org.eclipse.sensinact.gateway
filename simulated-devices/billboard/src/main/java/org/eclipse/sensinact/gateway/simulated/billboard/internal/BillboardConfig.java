/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.billboard.internal;

import java.util.ArrayList;
import java.util.List;

public class BillboardConfig {
    private List<BillboardConfigListener> listeners;
    private String message;

    public BillboardConfig() {
        this.message = "Hello sensiNact";
        this.listeners = new ArrayList<BillboardConfigListener>();
    }

    public void addListener(BillboardConfigListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BillboardConfigListener listener) {
        listeners.remove(listener);
    }

    private void updateMessage(String message) {
        for (BillboardConfigListener listener : listeners) {
            listener.messageChanged(message);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        this.updateMessage(message);
    }
}
