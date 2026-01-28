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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact;

import static org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings.EMPTY;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toDatastream;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toFeatureOfInterest;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toHistoricalLocation;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toLocation;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toObservation;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toObservedProperty;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toSensor;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.DtoMapper.toThing;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.AbstractDelegate;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class RootResourceDelegateSensinact extends AbstractDelegate {

    public RootResourceDelegateSensinact(UriInfo uriInfo, Providers providers, Application application,
            ContainerRequestContext requestContext) {
        super(uriInfo, providers, application, requestContext);
    }

    private List<ProviderSnapshot> listProvidersSeninact(final ICriterion criterion) {
        final SensiNactSession userSession = getSession();
        final List<ProviderSnapshot> providers = userSession.filteredSnapshot(criterion);
        if (criterion != null && criterion.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = criterion.getResourceValueFilter();
            return providers.stream().filter(p -> !DtoMapperSimple.isSensorthingModel(p)).filter(p -> rcFilter.test(p,
                    p.getServices().stream().flatMap(s -> s.getResources().stream()).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        } else {
            return providers.stream().filter(p -> !DtoMapperSimple.isSensorthingModel(p)).toList();
        }
    }

    private List<ResourceSnapshot> listSetResourcesSensinact(final ICriterion criterion) {
        return listResources(criterion).stream().filter(ResourceSnapshot::isSet)
                .filter(r -> !DtoMapperSimple.isSensorthingModel(r.getService().getProvider()))
                .collect(Collectors.toList());
    }

    private List<ResourceSnapshot> listResourcesSensinact(final ICriterion criterion) {

        final SensiNactSession userSession = getSession();
        List<ProviderSnapshot> providers = userSession.filteredSnapshot(criterion);
        if (criterion != null && criterion.getResourceValueFilter() != null) {
            final ResourceValueFilter rcFilter = criterion.getResourceValueFilter();
            return providers.stream().filter(p -> !DtoMapperSimple.isSensorthingModel(p))
                    .flatMap(p -> p.getServices().stream()).flatMap(s -> s.getResources().stream())
                    .filter(r -> rcFilter.test(r.getService().getProvider(), List.of(r))).collect(Collectors.toList());
        } else {
            return providers.stream().filter(p -> !DtoMapperSimple.isSensorthingModel(p))
                    .flatMap(p -> p.getServices().stream()).flatMap(s -> s.getResources().stream())
                    .collect(Collectors.toList());
        }
    }

    private static Optional<? extends ResourceSnapshot> getResource(final ProviderSnapshot provider,
            final String svcName, final String rcName) {
        return provider.getServices().stream().filter(s -> !DtoMapperSimple.isSensorthingModel(s.getProvider()))
                .filter(s -> s.getName().equals(svcName)).flatMap(s -> s.getResources().stream())
                .filter(r -> r.getName().equals(rcName)).findFirst();
    }

    private boolean hasResourceSet(final ProviderSnapshot provider, final String svcName, final String rcName) {
        if (provider == null) {
            return false;
        }

        Optional<? extends ResourceSnapshot> resource = getResource(provider, svcName, rcName);
        if (resource.isEmpty()) {
            return false;
        }
        if (DtoMapperSimple.isSensorthingModel(resource.get().getService().getProvider())) {
            return false;
        }

        return resource.get().isSet();
    }

    public ResultList<Thing> getThings() {
        ICriterion criterion = parseFilter(EFilterContext.THINGS);
        List<ProviderSnapshot> providers = listProvidersSeninact(criterion);
        return new ResultList<>(null, null,
                providers.stream().filter(p -> !"sensiNact".equals(p.getName())).map(
                        p -> toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, p))
                        .toList());
    }

    public ResultList<Location> getLocations() {
        ICriterion criterion = parseFilter(EFilterContext.LOCATIONS);
        List<ProviderSnapshot> providers = listProvidersSeninact(criterion);
        return new ResultList<>(null, null,
                providers.stream().filter(p -> hasResourceSet(p, "admin", "location")).map(
                        p -> toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, p))
                        .toList());
    }

    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ICriterion criterion = parseFilter(EFilterContext.HISTORICAL_LOCATIONS);
        List<ProviderSnapshot> providers = listProvidersSeninact(criterion);
        return new ResultList<>(null, null,
                providers.stream().filter(p -> hasResourceSet(p, "admin", "location"))
                        .map(p -> toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                criterion, p))
                        .filter(Optional::isPresent).map(Optional::get).toList());
    }

    public ResultList<Datastream> getDatastreams() {
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);
        List<ResourceSnapshot> resources = listSetResourcesSensinact(criterion);
        return new ResultList<>(null, null, resources.stream()
                .map(r -> toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(), r, criterion))
                .toList());
    }

    public ResultList<Sensor> getSensors() {
        ICriterion criterion = parseFilter(EFilterContext.SENSORS);
        List<ResourceSnapshot> resources = listSetResourcesSensinact(criterion);
        return new ResultList<>(null, null,
                resources.stream().map(
                        r -> toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, r))
                        .toList());
    }

    // No history as it is *live* observation data not a data stream

    public ResultList<Observation> getObservations() {
        ICriterion criterion = parseFilter(EFilterContext.OBSERVATIONS);
        List<ResourceSnapshot> resources = listSetResourcesSensinact(criterion);
        return new ResultList<>(null, null, resources.stream()
                .map(r -> toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, r))
                .filter(Optional::isPresent).map(Optional::get).toList());
    }

    public ResultList<ObservedProperty> getObservedProperties() {
        ICriterion criterion = parseFilter(EFilterContext.OBSERVED_PROPERTIES);
        List<ResourceSnapshot> resources = listSetResourcesSensinact(criterion);
        return new ResultList<>(null, null, resources.stream().map(
                r -> toObservedProperty(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, r))
                .toList());
    }

    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        ICriterion criterion = parseFilter(EFilterContext.FEATURES_OF_INTEREST);
        List<ProviderSnapshot> providers = listProvidersSeninact(criterion);
        return new ResultList<>(null, null, providers.stream().map(p -> toFeatureOfInterest(getSession(), application,
                getMapper(), uriInfo, getExpansions(), criterion, p)).toList());
    }

    static ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ResourceSnapshot resourceSnapshot, ICriterion filter) {
        ExpansionSettings es = (ExpansionSettings) requestContext.getProperty(IFilterConstants.EXPAND_SETTINGS_STRING);
        return getObservationList(userSession, application, mapper, uriInfo, es == null ? EMPTY : es, resourceSnapshot,
                filter, 0);
    }

    static ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ResourceSnapshot resourceSnapshot,
            ICriterion filter, int localResultLimit) {

        ResultList<Observation> list = HistoryResourceHelperSensinact.loadHistoricalObservations(userSession,
                application, mapper, uriInfo, expansions, resourceSnapshot, filter, localResultLimit);

        if (list.value().isEmpty() && resourceSnapshot.isSet()) {
            list = new ResultList<Observation>(null, null, DtoMapper
                    .toObservation(userSession, application, mapper, uriInfo, expansions, filter, resourceSnapshot)
                    .map(List::of).orElse(List.of()));
        }

        return list;
    }

}
