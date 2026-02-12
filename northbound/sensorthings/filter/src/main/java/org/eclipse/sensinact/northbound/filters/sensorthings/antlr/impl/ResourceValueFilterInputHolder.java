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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;

/**
 * Object given as input of filter predicates
 */
public class ResourceValueFilterInputHolder {

    private final EFilterContext context;
    private final ProviderSnapshot provider;
    private final List<? extends ResourceSnapshot> resources;
    private final ResourceSnapshot resource;
    private final SensiNactSession session;
    private Map<String, Object> configProperties;
    private final IDtoMemoryCache<ExpandedObservation> cacheObs;
    private final IDtoMemoryCache<Instant> cacheHl;

    /**
     *
     * @param session
     * @param context
     * @param provider
     * @param resources
     * @param configProperties
     */
    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ProviderSnapshot provider, final List<? extends ResourceSnapshot> resources,
            final Map<String, Object> configProperties, IDtoMemoryCache<ExpandedObservation> cacheObs,
            IDtoMemoryCache<Instant> cacheHl) {
        this.context = context;
        this.provider = provider;
        this.resources = resources;
        this.resource = null;
        this.session = session;
        this.configProperties = configProperties;
        this.cacheHl = cacheHl;
        this.cacheObs = cacheObs;

    }

    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ProviderSnapshot provider, final List<? extends ResourceSnapshot> resources,
            final Map<String, Object> configProperties) {
        this(context, session, provider, resources, configProperties, null, null);

    }

    /**
     *
     * @param context
     * @param session
     * @param provider
     * @param resource
     * @param configProperties
     */
    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ProviderSnapshot provider, final ResourceSnapshot resource,
            final Map<String, Object> configProperties, IDtoMemoryCache<ExpandedObservation> cacheObs,
            IDtoMemoryCache<Instant> cacheHl) {
        this.context = context;
        this.provider = provider;
        this.resources = List.of(resource);
        this.resource = resource;
        this.session = session;
        this.configProperties = configProperties;
        this.cacheHl = cacheHl;
        this.cacheObs = cacheObs;

    }

    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ProviderSnapshot provider, final ResourceSnapshot resource,
            final Map<String, Object> configProperties) {
        this(context, session, provider, resource, configProperties, null, null);

    }

    /**
     *
     * @param context
     * @param session
     * @param resource
     * @param configProperties
     */
    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ResourceSnapshot resource, final Map<String, Object> configProperties,
            IDtoMemoryCache<ExpandedObservation> cacheObs, IDtoMemoryCache<Instant> cacheHl) {
        this(context, session, resource.getService().getProvider(), resource, configProperties, cacheObs, cacheHl);
    }

    public ResourceValueFilterInputHolder(final EFilterContext context, SensiNactSession session,
            final ResourceSnapshot resource, final Map<String, Object> configProperties) {
        this(context, session, resource.getService().getProvider(), resource, configProperties, null, null);
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

    public Map<String, Object> getConfigProperties() {
        return configProperties;
    }

    public SensiNactSession getSession() {
        return session;
    }

    public IDtoMemoryCache<Instant> getCacheHl() {
        return cacheHl;
    }

    public IDtoMemoryCache<ExpandedObservation> getCacheObs() {
        return cacheObs;
    }
}
