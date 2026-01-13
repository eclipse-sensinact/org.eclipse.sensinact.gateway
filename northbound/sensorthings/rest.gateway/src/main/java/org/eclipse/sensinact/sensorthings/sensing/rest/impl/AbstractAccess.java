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
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
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

    protected List<ProviderSnapshot> getLocationProvidersFromThing(String thingId) {
        return getLinkProvidersFromThing(getSession(), thingId, "locationIds");
    }

    protected List<ResourceSnapshot> listSetResources(final ICriterion criterion) {
        return listResources(criterion).stream().filter(ResourceSnapshot::isSet).collect(Collectors.toList());
    }

    protected List<ProviderSnapshot> getLocationThingsProvider(String id) {
        /*
         * / TODO list of thing for this location String filterLocationInThing =
         * String.format(""" { "providers": [{ "model": "%s" "resources": { "service":
         * "%s" "resource": "locationIds" "value": "%s" } }] } """,
         * SENSOR_THING_DEVICE.getName(), UtilDto.SERVICE_THING, id); return
         * listProviders(parseFilter(filterLocationInThing, THINGS));
         */
        return List.of();
    }

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

    protected List<ResourceSnapshot> listResources(final ICriterion criterion) {

        final SensiNactSession userSession = getSession();
        List<ProviderSnapshot> providers = userSession.filteredSnapshot(criterion);
        if (criterion != null && criterion.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = criterion.getResourceValueFilter();
            return providers.stream().flatMap(p -> p.getServices().stream()).flatMap(s -> s.getResources().stream())
                    .filter(r -> rcFilter.test(r.getService().getProvider(), List.of(r))).collect(Collectors.toList());
        } else {
            return providers.stream().flatMap(p -> p.getServices().stream()).flatMap(s -> s.getResources().stream())
                    .collect(Collectors.toList());
        }
    }

    protected ResourceSnapshot getObservationResourceSnapshot(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(UtilDto.extractFirstIdSegment(id));
        ServiceSnapshot serviceSnapshot = UtilDto.getDatastreamService(providerSnapshot);
        ResourceSnapshot resourceSnapshot = serviceSnapshot.getResource("lastObservation");
        return resourceSnapshot;
    }

    protected String getThingIdFromDatastream(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);
        ProviderSnapshot providerDatastream = validateAndGetProvider(provider);
        ServiceSnapshot serviceDatastream = UtilDto.getDatastreamService(providerDatastream);
        String thingId = UtilDto.getResourceField(serviceDatastream, "thingId", String.class);
        return thingId;
    }

    protected static List<ProviderSnapshot> getLinkProvidersFromThing(SensiNactSession session, String thingId,
            String resourceField) {
        ProviderSnapshot providerThing = validateAndGetProvider(session, thingId);
        ServiceSnapshot serviceThing = UtilDto.getThingService(providerThing);
        List<?> linkIds = UtilDto.getResourceField(serviceThing, resourceField, List.class);
        List<ProviderSnapshot> providerLocations = linkIds.stream()
                .map(linkId -> validateAndGetProvider(session, (String) linkId)).toList();
        return providerLocations;
    }

    @SuppressWarnings("unchecked")
    protected static List<String> getLinkIdsFromThing(SensiNactSession session, String thingId, String resourceField) {
        ProviderSnapshot providerThing = validateAndGetProvider(session, thingId);
        ServiceSnapshot serviceThing = UtilDto.getThingService(providerThing);
        return UtilDto.getResourceField(serviceThing, resourceField, List.class);

    }

    protected static List<String> getLocationIdsFromThing(SensiNactSession session, String thingId) {
        return getLinkIdsFromThing(session, thingId, "locationIds");

    }

    protected boolean isLinkProvidersFromThing(String thingId, String subLinkId, String resourceField) {
        ProviderSnapshot providerThing = validateAndGetProvider(thingId);
        ServiceSnapshot serviceThing = UtilDto.getThingService(providerThing);
        return UtilDto.getResourceField(serviceThing, resourceField, List.class).contains(subLinkId);

    }

    protected boolean isLocationInThing(String thingId, String subLinkId) {
        return isLinkProvidersFromThing(thingId, subLinkId, "locationIds");

    }

    protected boolean isDatastreamInThing(String thingId, String subLinkId) {
        return isLinkProvidersFromThing(thingId, subLinkId, "datastreamIds");

    }

    protected static List<ProviderSnapshot> getDatastreamProvidersFromThing(SensiNactSession session, String thingId) {
        return getLinkProvidersFromThing(session, thingId, "datastreamIds");

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
    private Optional<ProviderSnapshot> getProviderSnapshot(String id) {
        return Optional.ofNullable(getSession().providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

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
    protected static ProviderSnapshot validateAndGetProvider(SensiNactSession session, String id) {
        DtoMapper.validatedProviderId(id);

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(session, id);

        if (providerSnapshot.isEmpty()) {
            throw new NotFoundException("Unknown provider");
        }
        return providerSnapshot.get();
    }

    protected ProviderSnapshot validateAndGetProvider(String id) {
        DtoMapper.validatedProviderId(id);

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(id);

        if (providerSnapshot.isEmpty()) {
            throw new NotFoundException("Unknown provider");
        }
        return providerSnapshot.get();
    }

    /**
     * validate and get the resource link to the id
     *
     * @param id
     * @return
     */
    protected ResourceSnapshot validateAndGetResourceSnapshot(String id) {
        String provider = UtilDto.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        String service = UtilDto.extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = UtilDto.extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

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
     * return criterion for filtering regarding filterString
     *
     * @param FilterString
     * @param context
     * @return
     * @throws WebApplicationException
     */
    protected ICriterion parseFilter(String filterString, final EFilterContext context) throws WebApplicationException {
        if (filterString == null || filterString.isBlank()) {
            return null;
        }
        try {
            return getFilterParser().parseFilter(filterString, context);
        } catch (FilterParserException e) {
            throw new BadRequestException("Error parsing filter", e);
        }
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
        return parseFilter(filterString, context);
    }
}
