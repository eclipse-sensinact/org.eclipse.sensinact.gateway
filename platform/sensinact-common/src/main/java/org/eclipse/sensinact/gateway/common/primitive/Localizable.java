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
