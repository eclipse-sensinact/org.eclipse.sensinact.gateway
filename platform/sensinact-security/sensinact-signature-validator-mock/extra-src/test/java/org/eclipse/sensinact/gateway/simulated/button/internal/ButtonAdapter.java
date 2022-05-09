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
