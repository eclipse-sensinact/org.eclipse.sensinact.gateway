/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/

package org.eclipse.sensinact.core.snapshot;

import java.util.List;

public interface ProviderSnapshot extends CommonProviderSnapshot {

    /**
     * Returns the list of services of the provider
     */
    List<ServiceSnapshot> getServices();

    /**
     * Returns the snapshot of the provider service with the given name
     *
     * @param name Name of the service
     * @return Service snapshot or null if unknown
     */
    ServiceSnapshot getService(String name);

    /**
     * Returns the snapshot of a resource of the given service
     *
     * @param service  Service name
     * @param resource Resource name
     * @return Resource snapshot or null
     */
    ResourceSnapshot getResource(String service, String resource);

    /**
     * Returns the snapshot of a resource of the given service
     *
     * @param service  Service name
     * @param resource Resource name
     * @return Resource snapshot or null
     */
    List<LinkedProviderSnapshot> getLinkedProviders();
}
