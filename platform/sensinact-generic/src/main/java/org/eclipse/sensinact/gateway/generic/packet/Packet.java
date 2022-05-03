/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.packet;

/**
 * A communication object
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Packet {
    /**
     * Returns the bytes array of this communication
     * packet
     *
     * @return the bytes array of this communication
     * packet
     */
    byte[] getBytes();
}
