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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings.EMPTY;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

public abstract class AbstractAccess {

    @Context
    protected UriInfo uriInfo;

    @Context
    protected Providers providers;

    @Context
    protected Application application;

    @Context
    protected ContainerRequestContext requestContext;

    protected List<ProviderSnapshot> listProviders(final ICriterion criterion) {
        final SensiNactSession userSession = getSession();
        final List<ProviderSnapshot> providers = userSession.filteredSnapshot(criterion);
        if (criterion != null && criterion.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = criterion.getResourceValueFilter();
            return providers
                    .stream().filter(p -> rcFilter.test(p, p.getServices().stream()
                            .flatMap(s -> s.getResources().stream()).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else {
            return providers;
        }
    }

    protected List<ServiceSnapshot> listServices(final ICriterion criterion) {

        final SensiNactSession userSession = getSession();
        List<ProviderSnapshot> providers = userSession.filteredSnapshot(criterion);
        if (criterion != null && criterion.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = criterion.getResourceValueFilter();
            return providers.stream().flatMap(p -> p.getServices().stream()).flatMap(s -> s.getResources().stream())
                    .filter(r -> {
                        return rcFilter.test(r.getService().getProvider(), List.of(r));
                    }).map(r -> r.getService()).collect(Collectors.toList());
        } else {
            return providers.stream().map(p -> p.getServices().stream()).flatMap(s -> s).toList();

        }
    }

    /**
     * Returns a user session
     */
    protected SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns an object mapper
     */
    protected ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }

    /**
     * return the expansion session for managing expand
     *
     * @return
     */
    protected ExpansionSettings getExpansions() {
        ExpansionSettings es = (ExpansionSettings) requestContext.getProperty(IFilterConstants.EXPAND_SETTINGS_STRING);
        return es == null ? EMPTY : es;
    }

    /**
     * return the Provider link to the id
     *
     * @param id
     * @return
     */
    private static Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        return Optional.ofNullable(session.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    /**
     * return the service ExtraDelegate that manage the extra (POST,PUT,DELETE) on
     * sensorthing entity
     *
     * @return
     */
    protected IExtraDelegate getExtraDelegate() {
        ContextResolver<IExtraDelegate> contextResolver = providers.getContextResolver(IExtraDelegate.class,
                MediaType.WILDCARD_TYPE);
        if (contextResolver == null || contextResolver.getContext(null) == null) {
            throw new WebApplicationException("operation PUT/POST/DELET not available", 405);
        }
        return contextResolver.getContext(null);

    }

    /**
     * validate and get provider link to the id
     *
     * @param id
     * @return
     */

    protected ProviderSnapshot validateAndGetProvider(String id) {
        return DtoMapper.validateAndGetProvider(getSession(), id);
    }

    /**
     * validate and get provider link to the id
     *
     * @param id
     * @return
     */

    protected ServiceSnapshot validateAndGeService(String id, String serviceName) {
        return DtoMapper.validateAndGeService(getSession(), id, serviceName);
    }

    /**
     * validate and get the resource link to the id
     *
     * @param id
     * @return
     */

    protected ResourceSnapshot validateAndGetResourceSnapshot(String id) {
        return DtoMapper.validateAndGetResourceSnapshot(getSession(), id);
    }

    /**
     * get filterParser
     *
     * @return
     */
    private ISensorthingsFilterParser getFilterParser() {
        return providers.getContextResolver(ISensorthingsFilterParser.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * return criterion for filtering regarding the context
     *
     * @param context
     * @return
     * @throws WebApplicationException
     */
    protected ICriterion parseFilter(final EFilterContext context) throws WebApplicationException {
        final String filterString = (String) requestContext.getProperty(IFilterConstants.PROP_FILTER_STRING);
        if (filterString == null || filterString.isBlank()) {
            return null;
        }

        try {
            return getFilterParser().parseFilter(filterString, context);
        } catch (FilterParserException e) {
            throw new BadRequestException("Error parsing filter", e);
        }
    }

    protected IDtoMemoryCache<?> getCache(Class<?> dtoClass) {
        @SuppressWarnings("rawtypes")
        ContextResolver<IDtoMemoryCache> resolver = providers.getContextResolver(IDtoMemoryCache.class,
                MediaType.WILDCARD_TYPE);
        if (resolver == null) {
            return null;
        }
        IDtoMemoryCache<?> cache = resolver.getContext(dtoClass);
        if (cache == null) {
            throw new WebApplicationException(
                    String.format("cache for class %s doesn't exists", dtoClass.getSimpleName()));
        }
        return cache;
    }
}
