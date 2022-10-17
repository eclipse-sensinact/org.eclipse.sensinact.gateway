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
 * A PathElement service belongs to a hierarchy
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface PathElement {
    /**
     * Returns the string uri path of this
     * PathElement service
     *
     * @return the uri path of this PathElement
     * service
     */
    String getPath();
}
