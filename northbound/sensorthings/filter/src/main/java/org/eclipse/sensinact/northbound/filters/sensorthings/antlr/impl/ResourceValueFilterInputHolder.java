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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

/**
 * Object given as input of filter predicates
 */
public class ResourceValueFilterInputHolder {

    private final EFilterContext context;
    private final ProviderSnapshot provider;
    private final List<? extends ResourceSnapshot> resources;
    private final ResourceSnapshot resource;
    private final SensiNactSession session;

    /**
     * Provider filter input
     *
     * @param context   Query context
     * @param provider  Provider being filtered
     * @param resources Resources of the provider
     */
    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ProviderSnapshot provider, final List<? extends ResourceSnapshot> resources) {
        this.context = context;
        this.provider = provider;
        this.resources = resources;
        this.resource = null;
        this.session = session;

    }

    /**
     * Resource filter input
     *
     * @param context  Query context
     * @param provider Provider of the resource being filtered
     * @param resource Resource being filtered
     */
    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ProviderSnapshot provider, final ResourceSnapshot resource) {
        this.context = context;
        this.provider = provider;
        this.resources = List.of(resource);
        this.resource = resource;
        this.session = session;
    }

    /**
     * Resource filter input
     *
     * @param context  Query context
     * @param resource Resource being filtered
     */
    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ResourceSnapshot resource) {
        this(context, session, resource.getService().getProvider(), resource);
    }

    @Override
    public String toString() {
        return String
                .format("Holder(%s -> %s)", provider.getName(),
                        resources.stream()
                                .map(r -> String.format("%s/%s/%s=%s", r.getService().getProvider().getName(),
                                        r.getService().getName(), r.getName(), r.getValue()))
                                .collect(Collectors.toList()));
    }

    public EFilterContext getContext() {
        return context;
    }

    public ProviderSnapshot getProvider() {
        return provider;
    }

    public List<? extends ResourceSnapshot> getResources() {
        return resources;
    }

    public ResourceSnapshot getResource() {
        return resource;
    }

    public SensiNactSession getSession() {
        return session;
    }
}
