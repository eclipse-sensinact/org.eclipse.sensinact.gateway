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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.extractFirstIdSegment;

import java.util.EnumSet;
import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
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
    private Optional<ProviderSnapshot> getProviderSnapshot(String id) {
        return Optional.ofNullable(getSession().providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
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
        DtoMapperGet.validatedProviderId(id);

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(id);

        if (providerSnapshot.isEmpty()) {
            throw new NotFoundException("Unknown provider");
        }
        return providerSnapshot.get();
    }

    /**
     * validate and get provider link to the id
     *
     * @param id
     * @return
     */
    protected ServiceSnapshot validateAndGeService(String id) {
        String providerId = UtilIds.extractFirstIdSegment(id);
        String serviceId = UtilIds.extractSecondIdSegment(id);

        Optional<ProviderSnapshot> provider = DtoMapper.getProviderSnapshot(getSession(), providerId);

        if (provider != null && provider.isPresent() && serviceId != null) {
            return DtoMapper.getServiceSnapshot(provider.get(), serviceId);
        }
        throw new NotFoundException(String.format("can't find model identified by %s", id));
    }

    /**
     * validate and get the resource link to the id
     *
     * @param id
     * @return
     */
    protected ResourceSnapshot validateAndGetResourceSnapshot(String id) {
        String provider = extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResourceSnapshot resourceSnapshot = providerSnapshot.getResource(service, resource);

        if (resourceSnapshot == null) {
            throw new NotFoundException();
        }
        return resourceSnapshot;
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
}
