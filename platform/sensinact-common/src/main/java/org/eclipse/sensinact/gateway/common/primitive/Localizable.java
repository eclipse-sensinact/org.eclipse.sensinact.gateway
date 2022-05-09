/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.primitive;

/**
 * A localizable service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Localizable {
    /**
     * Returns the string location value of this
     * localizable service
     *
     * @return the string value of this localizable
     * service
     */
    String getLocation();

    /**
     * Sets the string location value of this localizable
     * service
     *
     * @param location the string location value to set
     * @return the string location value of this localizable
     * service
     * @throws InvalidValueException
     */
    String setLocation(String location) throws InvalidValueException;
}
