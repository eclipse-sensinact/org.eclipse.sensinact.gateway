/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */

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
        for(BillboardConfigListener listener : listeners) {
            listener.messageChanged(message);
        }
    }

    public void setMessage(String message) {
        this.message = message;
        this.updateMessage(message);
    }
}
