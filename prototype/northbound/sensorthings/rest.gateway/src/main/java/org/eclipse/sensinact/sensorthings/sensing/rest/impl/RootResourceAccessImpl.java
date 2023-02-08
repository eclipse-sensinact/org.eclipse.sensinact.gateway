/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.prototype.ProviderDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.snapshot.ICriterion;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;
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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
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
        if(filterString == null || filterString.isBlank()) {
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

    @Override
    public ResultList<Thing> getThings() {
        SensiNactSession userSession = getSession();
        ResultList<Thing> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.THINGS);
        list.value = providers.stream().map(p -> DtoMapper.toThing(userSession, uriInfo, p.getName())).collect(toList());

        return list;
    }

    @Override
    public ResultList<Location> getLocations() {
        SensiNactSession userSession = getSession();
        ResultList<Location> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream().map(p -> DtoMapper.toLocation(userSession, uriInfo, getMapper(), p.provider)).collect(toList());

        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocations() {
        SensiNactSession userSession = getSession();
        ResultList<HistoricalLocation> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .map(p -> DtoMapper.toHistoricalLocation(userSession, getMapper(), uriInfo, p.provider))
                .collect(toList());
        return list;
    }

    @Override
    public ResultList<Datastream> getDatastreams() {
        SensiNactSession userSession = getSession();
        ResultList<Datastream> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .flatMap(p -> p.services.stream().map(s -> userSession.describeService(p.provider, s)))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toDatastream(userSession, getMapper(), uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<Sensor> getSensors() {
        SensiNactSession userSession = getSession();
        ResultList<Sensor> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .flatMap(p -> p.services.stream().map(s -> userSession.describeService(p.provider, s)))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toSensor(uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<Observation> getObservations() {
        SensiNactSession userSession = getSession();
        ResultList<Observation> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .flatMap(p -> p.services.stream().map(s -> userSession.describeService(p.provider, s)))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toObservation(uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<ObservedProperty> getObservedProperties() {
        SensiNactSession userSession = getSession();
        ResultList<ObservedProperty> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .flatMap(p -> p.services.stream().map(s -> userSession.describeService(p.provider, s)))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toObservedProperty(uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        SensiNactSession userSession = getSession();
        ResultList<FeatureOfInterest> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .map(p -> DtoMapper.toFeatureOfInterest(userSession, uriInfo, getMapper(), p.provider)).collect(toList());

        return list;
    }

}
