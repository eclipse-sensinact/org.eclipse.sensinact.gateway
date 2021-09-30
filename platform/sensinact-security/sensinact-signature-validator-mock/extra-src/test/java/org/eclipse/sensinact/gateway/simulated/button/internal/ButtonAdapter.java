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
package org.eclipse.sensinact.gateway.simulated.button.internal;


/**
 *
 */
public class ButtonAdapter {
    /**
     * @param value
     */
    public void mouseReleased(boolean value) {
       System.out.println("VALUE :" + value);
    }
}
