/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.bundle;

import java.util.Dictionary;

/**
 * Listener of a {@link MediatorManagedConfiguration} properties
 * set update
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ManagedConfigurationListener {
    /**
     * Notification of an update of the properties set
     *
     * @param properties the updated set of properties
     */
    void updated(Dictionary<String, ?> properties);
}
