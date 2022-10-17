/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.button.internal;

import org.eclipse.sensinact.gateway.simulated.button.api.ButtonSetterItf;

public class ButtonSetter implements ButtonSetterItf {
    private final ButtonAdapter listener;

    public ButtonSetter(ButtonAdapter listener) {
        this.listener = listener;
    }

    public void move(boolean value) {
        this.listener.mouseReleased(value);
    }

    public void stop() {
    }
}
