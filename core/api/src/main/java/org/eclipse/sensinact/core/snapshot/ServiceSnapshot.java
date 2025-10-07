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

public interface ServiceSnapshot extends Snapshot {

    /**
     * Returns the snapshot of the parent provider
     */
    ProviderSnapshot getProvider();

    /**
     * Returns the list of service resources
     */
    List<ResourceSnapshot> getResources();

    /**
     * Returns the snapshot of the service resource with the given name
     *
     * @param name Resource name
     * @return Resource snapshot, null if unknown
     */
    ResourceSnapshot getResource(String name);
}
