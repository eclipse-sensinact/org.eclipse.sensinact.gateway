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

/**
 * Filters a provider snapshot according to the value of its resources
 */
public interface ResourceValueFilter {

    /**
     * Filters a provider snapshot according to the value of its resources
     *
     * @param provider  Snapshot of the filtered provider
     * @param resources Valued snapshot of all the resources of the filtered
     *                  provider
     * @return True if the provider is accepted, else false
     */
    boolean test(ProviderSnapshot provider, List<? extends ResourceSnapshot> resources);
}
