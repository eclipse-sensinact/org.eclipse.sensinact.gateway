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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.snapshot.ICriterion;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.prototype.twin.TimedValue;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.RootResourceAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class RootResourceAccessImpl implements RootResourceAccess {

    @Context
    UriInfo uriInfo;

    @Context
    Providers providers;

    @Context
    ContainerRequestContext requestContext;

    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    private SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    private ISensorthingsFilterParser getFilterParser() {
        return providers.getContextResolver(ISensorthingsFilterParser.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    private ICriterion parseFilter(final EFilterContext context) throws WebApplicationException {
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

    private List<ProviderSnapshot> listProviders(final EFilterContext context) {
        final SensiNactSession userSession = getSession();
        final ICriterion criterion = parseFilter(context);
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

    private List<ResourceSnapshot> listResources(EFilterContext context) {

        final SensiNactSession userSession = getSession();
        final ICriterion criterion = parseFilter(context);
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

    @Override
    public ResultList<Thing> getThings() {
        ResultList<Thing> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.THINGS);
        list.value = providers.stream().map(p -> DtoMapper.toThing(uriInfo, p)).collect(toList());

        return list;
    }

    @Override
    public ResultList<Location> getLocations() {
        ResultList<Location> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.LOCATIONS);
        list.value = providers.stream().map(p -> DtoMapper.toLocation(uriInfo, getMapper(), p)).collect(toList());

        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ResultList<HistoricalLocation> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.HISTORICAL_LOCATIONS);
        list.value = providers.stream().map(p -> DtoMapper.toHistoricalLocation(getMapper(), uriInfo, p))
                .collect(toList());
        return list;
    }

    @Override
    public ResultList<Datastream> getDatastreams() {
        ResultList<Datastream> list = new ResultList<>();

        List<ResourceSnapshot> resources = listResources(EFilterContext.DATASTREAMS);
        list.value = resources.stream().map(r -> DtoMapper.toDatastream(getMapper(), uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<Sensor> getSensors() {
        ResultList<Sensor> list = new ResultList<>();

        List<ResourceSnapshot> resources = listResources(EFilterContext.SENSORS);
        list.value = resources.stream().map(r -> DtoMapper.toSensor(uriInfo, r)).collect(toList());

        return list;
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getObservations() {
        ResultList<Observation> list = new ResultList<>();

        List<ResourceSnapshot> resources = listResources(EFilterContext.OBSERVATIONS);
        list.value = resources.stream().map(r -> DtoMapper.toObservation(uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<ObservedProperty> getObservedProperties() {
        ResultList<ObservedProperty> list = new ResultList<>();

        List<ResourceSnapshot> resources = listResources(EFilterContext.OBSERVED_PROPERTIES);
        list.value = resources.stream().map(r -> DtoMapper.toObservedProperty(uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        ResultList<FeatureOfInterest> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.FEATURES_OF_INTEREST);
        list.value = providers.stream().map(p -> DtoMapper.toFeatureOfInterest(uriInfo, getMapper(), p))
                .collect(toList());

        return list;
    }

    @SuppressWarnings("unchecked")
    static ResultList<Observation> getObservationList(SensiNactSession userSession, UriInfo uriInfo,
            Application application, String provider, String service, String resource) {

        ResourceDescription rd;
        try {
            rd = userSession.describeResource(provider, service, resource);
        } catch (IllegalArgumentException iae) {
            throw new NotFoundException(iae);
        }

        ResultList<Observation> list = new ResultList<>();

        String historyProvider = (String) application.getProperties().get("sensinact.history.provider");
        Integer maxResults = (Integer) application.getProperties().get("sensinact.history.result.limit");

        List<Observation> results = new ArrayList<>();

        if (historyProvider != null) {
            Long count = (Long) userSession.actOnResource(historyProvider, "history", "count",
                    Map.of("provider", provider, "service", service, "resource", resource));

            Map<String, Object> params = new HashMap<>(
                    Map.of("provider", provider, "service", service, "resource", resource));
            Integer skip = Integer.valueOf(0);

            List<TimedValue<?>> timed;
            do {
                params.put("skip", skip);

                timed = (List<TimedValue<?>>) userSession.actOnResource(historyProvider, "history", "range", params);

                results.addAll(0, DtoMapper.toObservationList(uriInfo, provider, service, resource, timed));

                if (timed.isEmpty()) {
                    break;
                } else if (timed.size() == 500) {
                    skip = results.size();
                }

            } while (results.size() < count && results.size() < maxResults);
        }

        if (results.isEmpty()) {
            results.add(DtoMapper.toObservation(uriInfo, rd));
        }

        list.value = results;

        return list;
    }

}
