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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;

/**
 * @author thoma
 *
 */
public class ResourceValueFilterInputHolder {

    private final ProviderSnapshot provider;
    private final List<ResourceSnapshot> resources;

    public ResourceValueFilterInputHolder(final ProviderSnapshot provider, final List<ResourceSnapshot> resources) {
        this.provider = provider;
        this.resources = resources;
    }

    @Override
    public String toString() {
        return String.format("Holder(%s -> %s)", provider.getName(),
                resources.stream().map(r -> String.format("%s/%s/%s", r.getService().getProvider().getName(),
                        r.getService().getName(), r.getName())).collect(Collectors.toList()));
    }

    /**
     * @return the provider
     */
    public ProviderSnapshot getProvider() {
        return provider;
    }

    /**
     * @return the resources
     */
    public List<ResourceSnapshot> getResources() {
        return resources;
    }
}
