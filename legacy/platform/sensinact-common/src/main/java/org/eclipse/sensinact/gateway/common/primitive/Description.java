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
 * A Description service provides a complete JSON formated
 * description string of an other service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Description extends JSONable, Nameable {
    /**
     * Returns the JSON formated description string of the
     * object described by this Description instance. Unlike
     * the inherited {@link #getJSON()} which is context
     * dependent the description returned by this method
     * should be "complete"
     *
     * @return the complete JSON formated description string
     * of the object described by this Description
     */
    String getJSONDescription();
}
