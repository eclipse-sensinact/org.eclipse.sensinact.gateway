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

package org.eclipse.sensinact.gateway.simulated.button.internal;

import org.eclipse.sensinact.gateway.simulated.button.api.ButtonSetterItf;

public class ButtonSetter implements ButtonSetterItf {
	private final ButtonAdapter listener;

    public ButtonSetter(ButtonAdapter listener) {
    	this.listener = listener;
    }

    public void move(boolean value)
    {
        this.listener.mouseReleased(value);
    }

    public void stop() {}
}
