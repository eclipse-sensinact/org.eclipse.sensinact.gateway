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
 * A JSONable service provides its own description as
 * a JSON formated string
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface JSONable {
    /**
     * Returns the JSON formated String description
     * of this JSONable implementation instance
     *
     * @return the JSON formated string description
     */
    String getJSON();
}
