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

import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.HISTORICAL_LOCATIONS;
import static org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext.THINGS;
import static org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings.EMPTY;

import java.net.URI;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IDtoMemoryCache;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.DtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

public abstract class AbstractDelegate {

    protected UriInfo uriInfo;

    protected Providers providers;

    protected Application application;

    protected ContainerRequestContext requestContext;

    public AbstractDelegate(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        this.uriInfo = uriInfo;
        this.providers = providers;
        this.application = application;
        this.requestContext = requestContext;
    }

    protected boolean isHistoryMemory() {
        Object flag = application.getProperties().get("sensinact.history.in.memory");
        if (flag != null)
            return (boolean) flag;
        return false;

    }

    public Thing getThing(String id) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(id);

        return DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), parseFilter(THINGS),
                providerSnapshot);
    }

    public HistoricalLocation getHistoricalLocationFromThing(String id) {

        String provider = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);
        try {
            Optional<HistoricalLocation> historicalLocation = DtoMapper.toHistoricalLocation(getSession(), application,
                    getMapper(), uriInfo, getExpansions(), parseFilter(HISTORICAL_LOCATIONS), providerSnapshot);
            if (historicalLocation.isEmpty()) {
                throw new NotFoundException();
            }
            return historicalLocation.get();
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException("No feature of interest with id");
        }
    }

    public HistoricalLocation getHistoricalLocation(String id) {

        HistoricalLocation historicalLocation = getHistoricalLocationFromThing(id);
        if (!historicalLocation.id().equals(id)) {
            throw new NotFoundException();
        }
        return historicalLocation;
    }

    public URI getCreatedUri(Self createDto) {
        URI createdUri = URI.create(createDto.selfLink());
        return createdUri;
    }

    protected List<ProviderSnapshot> getLocationProvidersFromThing(String thingId) {
        return getLinkProvidersFromThing(getSession(), thingId, "locationIds");
    }

    public Response updateThing(String id, ExpandedThing thing) {

        ProviderSnapshot snapshot = getExtraDelegate().update(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), id, thing);
        ICriterion criterion = parseFilter(EFilterContext.THINGS);

        Thing createDto = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion,
                snapshot);

        return Response.ok().entity(createDto).build();
    }

    protected List<ResourceSnapshot> listSetResources(final ICriterion criterion) {
        return listResources(criterion).stream().filter(ResourceSnapshot::isSet).collect(Collectors.toList());
    }

    protected List<ProviderSnapshot> getLocationThingsProvider(String id) {
        return listProviders(parseFilter(EFilterContext.THINGS)).stream().map(DtoMapperSimple::getThingService)
                .filter(Objects::nonNull)
                .filter(s -> DtoMapperSimple.getResourceField(s, "locationIds", List.class).contains(id))
                .map(s -> s.getProvider()).toList();
    }

    public static List<ProviderSnapshot> getLocationThingsProvider(SensiNactSession session, String id) {
        return listProviders(session, null).stream().map(DtoMapperSimple::getThingService).filter(Objects::nonNull)
                .filter(s -> DtoMapperSimple.getResourceField(s, "locationIds", List.class).contains(id))
                .map(s -> s.getProvider()).toList();
    }

    protected List<ProviderSnapshot> listProviders(final ICriterion criterion) {
        final SensiNactSession userSession = getSession();
        return listProviders(userSession, criterion);

    }

    protected static List<ProviderSnapshot> listProviders(SensiNactSession session, final ICriterion criterion) {
        return session.filteredSnapshot(criterion);

    }

    @SuppressWarnings("unchecked")
    protected IDtoMemoryCache<ExpandedObservation> getCacheObservation() {
        return providers.getContextResolver(IDtoMemoryCache.class, MediaType.WILDCARD_TYPE)
                .getContext(ExpandedObservation.class);
    }

    @SuppressWarnings("unchecked")
    protected IDtoMemoryCache<FeatureOfInterest> getCacheFeatureOfInterest() {
        return providers.getContextResolver(IDtoMemoryCache.class, MediaType.WILDCARD_TYPE)
                .getContext(FeatureOfInterest.class);
    }

    @SuppressWarnings("unchecked")
    protected IDtoMemoryCache<Instant> getCacheHistoricalLocation() {
        return providers.getContextResolver(IDtoMemoryCache.class, MediaType.WILDCARD_TYPE).getContext(Instant.class);
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
        ProviderSnapshot providerSnapshot = validateAndGetProvider(DtoMapperSimple.extractFirstIdSegment(id));
        ServiceSnapshot serviceSnapshot = DtoMapperSimple.getDatastreamService(providerSnapshot);
        ResourceSnapshot resourceSnapshot = serviceSnapshot.getResource("lastObservation");

        return resourceSnapshot;
    }

    protected String getThingIdFromDatastream(String id) {
        String provider = DtoMapperSimple.extractFirstIdSegment(id);
        ProviderSnapshot providerDatastream = validateAndGetProvider(provider);
        ServiceSnapshot serviceDatastream = DtoMapperSimple.getDatastreamService(providerDatastream);
        String thingId = DtoMapperSimple.getResourceField(serviceDatastream, "thingId", String.class);
        return thingId;
    }

    protected static List<ProviderSnapshot> getLinkProvidersFromThing(SensiNactSession session, String thingId,
            String resourceField) {
        ProviderSnapshot providerThing = validateAndGetProvider(session, thingId);
        ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(providerThing);
        List<?> linkIds = DtoMapperSimple.getResourceField(serviceThing, resourceField, List.class);
        List<ProviderSnapshot> providerLocations = linkIds.stream()
                .map(linkId -> validateAndGetProvider(session, (String) linkId)).toList();
        return providerLocations;
    }

    @SuppressWarnings("unchecked")
    protected static List<String> getLinkIdsFromThing(SensiNactSession session, String thingId, String resourceField) {
        ProviderSnapshot providerThing = validateAndGetProvider(session, thingId);
        ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(providerThing);
        return DtoMapperSimple.getResourceField(serviceThing, resourceField, List.class);

    }

    protected static List<String> getLocationIdsFromThing(SensiNactSession session, String thingId) {
        return getLinkIdsFromThing(session, thingId, "locationIds");

    }

    protected boolean isLinkProvidersFromThing(String thingId, String subLinkId, String resourceField) {
        ProviderSnapshot providerThing = validateAndGetProvider(thingId);
        ServiceSnapshot serviceThing = DtoMapperSimple.getThingService(providerThing);
        return DtoMapperSimple.getResourceField(serviceThing, resourceField, List.class).contains(subLinkId);

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
    public Optional<ProviderSnapshot> getProviderSnapshot(String id) {
        return Optional.ofNullable(getSession().providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    private static Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        String idProvider = DtoMapperSimple.extractFirstIdSegment(id);

        return Optional.ofNullable(session.providerSnapshot(idProvider, EnumSet.noneOf(SnapshotOption.class)));
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
        String provider = DtoMapperSimple.extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        String service = DtoMapperSimple.extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = DtoMapperSimple.extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

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
