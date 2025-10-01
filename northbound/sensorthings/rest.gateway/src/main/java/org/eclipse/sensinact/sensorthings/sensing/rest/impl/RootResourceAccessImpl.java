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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toDatastream;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toFeatureOfInterest;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toHistoricalLocation;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toLocation;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toObservation;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toObservedProperty;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toSensor;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.toThing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.RootResourceAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

public class RootResourceAccessImpl extends AbstractAccess implements RootResourceAccess {

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

    private List<ResourceSnapshot> listSetResources(EFilterContext context) {
        return listResources(context).stream().filter(ResourceSnapshot::isSet).collect(Collectors.toList());
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

    private static Optional<? extends ResourceSnapshot> getResource(final ProviderSnapshot provider, final String svcName,
            final String rcName) {
        return provider.getServices().stream().filter(s -> s.getName().equals(svcName))
                .flatMap(s -> s.getResources().stream()).filter(r -> r.getName().equals(rcName)).findFirst();
    }

    private boolean hasResourceSet(final ProviderSnapshot provider, final String svcName, final String rcName) {
        if (provider == null) {
            return false;
        }

        Optional<? extends ResourceSnapshot> resource = getResource(provider, svcName, rcName);
        if (resource.isEmpty()) {
            return false;
        }

        return resource.get().isSet();
    }

    @Override
    public ResultList<Thing> getThings() {
        ResultList<Thing> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.THINGS);
        list.value = providers.stream()
                .map(p -> toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), p))
                .collect(toList());

        return list;
    }

    @Override
    public ResultList<Location> getLocations() {
        ResultList<Location> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.LOCATIONS);
        list.value = providers.stream()
                .filter(p -> hasResourceSet(p, "admin", "location"))
                .map(p -> toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(), p))
                .collect(toList());

        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ResultList<HistoricalLocation> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.HISTORICAL_LOCATIONS);
        list.value = providers.stream()
                .filter(p -> hasResourceSet(p, "admin", "location"))
                .map(p -> toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(), p))
                .collect(toList());
        return list;
    }

    @Override
    public ResultList<Datastream> getDatastreams() {
        ResultList<Datastream> list = new ResultList<>();

        List<ResourceSnapshot> resources = listSetResources(EFilterContext.DATASTREAMS);
        list.value = resources.stream()
                .map(r -> toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(), r))
                .collect(toList());

        return list;
    }

    @Override
    public ResultList<Sensor> getSensors() {
        ResultList<Sensor> list = new ResultList<>();

        List<ResourceSnapshot> resources = listSetResources(EFilterContext.SENSORS);
        list.value = resources.stream().
                map(r -> toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(), r))
                .collect(toList());

        return list;
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getObservations() {
        ResultList<Observation> list = new ResultList<>();

        List<ResourceSnapshot> resources = listSetResources(EFilterContext.OBSERVATIONS);
        list.value = resources.stream()
                .map(r -> toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(), r))
                .collect(toList());

        return list;
    }

    @Override
    public ResultList<ObservedProperty> getObservedProperties() {
        ResultList<ObservedProperty> list = new ResultList<>();

        List<ResourceSnapshot> resources = listSetResources(EFilterContext.OBSERVED_PROPERTIES);
        list.value = resources.stream()
                .map(r -> toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(), r))
                .collect(toList());

        return list;
    }

    @Override
    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        ResultList<FeatureOfInterest> list = new ResultList<>();

        List<ProviderSnapshot> providers = listProviders(EFilterContext.FEATURES_OF_INTEREST);
        list.value = providers.stream()
                .map(p -> toFeatureOfInterest(getSession(), application, getMapper(), uriInfo, getExpansions(), p))
                .collect(toList());

        return list;
    }

    static ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resourceSnapshot,
            int localResultLimit) {

        ResultList<Observation> list = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                mapper, uriInfo, expansions, resourceSnapshot, localResultLimit);

        if (list.value.isEmpty() && resourceSnapshot.isSet()) {
            list.value.add(DtoMapper.toObservation(userSession, application, mapper, uriInfo, expansions, resourceSnapshot));
        }

        return list;
    }

}
