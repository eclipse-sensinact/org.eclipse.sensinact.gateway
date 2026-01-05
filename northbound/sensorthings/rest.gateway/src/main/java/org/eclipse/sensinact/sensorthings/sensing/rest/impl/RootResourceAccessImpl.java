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
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet.toHistoricalLocation;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.RootResourceAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.RootResourceCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

public class RootResourceAccessImpl extends AbstractAccess implements RootResourceAccess, RootResourceCreate {

    private static Optional<? extends ResourceSnapshot> getResource(final ProviderSnapshot provider,
            final String svcName, final String rcName) {
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
        ICriterion criterion = parseFilter(EFilterContext.THINGS);
        List<ProviderSnapshot> providers = listProviders(criterion);
        return new ResultList<>(null, null,
                providers.stream().map(p -> UtilIds.getThingService(p)).filter(Objects::nonNull).map(s -> DtoMapper
                        .toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, s))
                        .toList());
    }

    @Override
    public ResultList<Location> getLocations() {
        ICriterion criterion = parseFilter(EFilterContext.LOCATIONS);
        ICriterion criterionThing = parseFilter(EFilterContext.THINGS);
        List<ProviderSnapshot> providers = listProviders(criterion);
        return new ResultList<>(null, null,
                providers.stream().map(p -> UtilIds.getLocationService(p)).filter(Objects::nonNull)
                        .map(s -> DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                criterion, s, criterionThing))
                        .toList());
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ICriterion criterion = parseFilter(EFilterContext.HISTORICAL_LOCATIONS);
        List<ProviderSnapshot> providers = listProviders(criterion);
        return new ResultList<>(null, null,
                providers.stream().filter(p -> hasResourceSet(p, "admin", "location"))
                        .map(p -> toHistoricalLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                                criterion, p))
                        .filter(Optional::isPresent).map(Optional::get).toList());
    }

    @Override
    public ResultList<Datastream> getDatastreams() {
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);
        List<ServiceSnapshot> serviceSnapshots = listServices(criterion);
        return new ResultList<>(null, null,
                serviceSnapshots.stream().filter(s -> UtilIds.SERVICE_DATASTREAM.equals(s.getName())).map(s -> DtoMapper
                        .toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, s))
                        .toList());
    }

    @Override
    public ResultList<Sensor> getSensors() {
        ICriterion criterion = parseFilter(EFilterContext.SENSORS);
        List<ServiceSnapshot> servicesDatastreams = listServices(criterion);
        return new ResultList<>(null, null, servicesDatastreams
                .stream().filter(s -> UtilIds.SERVICE_DATASTREAM.equals(s.getName())).map(s -> DtoMapper
                        .toSensor(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, s))
                .toList());
    }

    // No history as it is *live* observation data not a data stream
    @Override
    public ResultList<Observation> getObservations() {
        ICriterion criterion = parseFilter(EFilterContext.OBSERVATIONS);
        List<ServiceSnapshot> services = listServices(criterion);

        return new ResultList<>(null, null,
                services.stream().filter(s -> UtilIds.SERVICE_DATASTREAM.equals(s.getName())).map(s -> DtoMapper
                        .toObservation(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion, s))
                        .toList());
    }

    @Override
    public ResultList<ObservedProperty> getObservedProperties() {
        ICriterion criterion = parseFilter(EFilterContext.OBSERVED_PROPERTIES);
        List<ServiceSnapshot> services = listServices(criterion);

        return new ResultList<>(null, null,
                services.stream().filter(s -> UtilIds.SERVICE_DATASTREAM.equals(s.getName()))
                        .map(s -> DtoMapper.toObservedProperty(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), criterion, s))
                        .toList());
    }

    @Override
    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        ICriterion criterion = parseFilter(EFilterContext.FEATURES_OF_INTEREST);
        List<ServiceSnapshot> providers = listServices(criterion);
        return new ResultList<>(null, null,
                providers.stream().filter(s -> UtilIds.SERVICE_DATASTREAM.equals(s.getName())).filter(Objects::nonNull)
                        .map(s -> DtoMapper.toFeatureOfInterest(getSession(), application, getMapper(), uriInfo,
                                getExpansions(), criterion, s))
                        .toList());
    }

    static ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ContainerRequestContext requestContext,
            ServiceSnapshot serviceSnapshot, ICriterion filter) {
        ExpansionSettings es = (ExpansionSettings) requestContext.getProperty(IFilterConstants.EXPAND_SETTINGS_STRING);
        return getObservationList(userSession, application, mapper, uriInfo, es == null ? EMPTY : es, serviceSnapshot,
                filter, 0);
    }

    static ResultList<Observation> getObservationList(SensiNactSession userSession, Application application,
            ObjectMapper mapper, UriInfo uriInfo, ExpansionSettings expansions, ServiceSnapshot serviceSnapshot,
            ICriterion filter, int localResultLimit) {
        // TODO
        ResultList<Observation> list = HistoryResourceHelper.loadHistoricalObservations(userSession, application,
                mapper, uriInfo, expansions, null, filter, localResultLimit);

        if (list.value().isEmpty()) {
            list = new ResultList<Observation>(null, null, List.of(DtoMapper.toObservation(userSession, application,
                    mapper, uriInfo, expansions, filter, serviceSnapshot)));
        }

        return list;
    }

    @Override
    public Response createDatastream(ExpandedDataStream datastream) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), datastream);
        ICriterion criterion = parseFilter(EFilterContext.DATASTREAMS);

        Datastream createDto = DtoMapper.toDatastream(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot);
        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();
    }

    @Override
    public Response createFeaturesOfInterest(FeatureOfInterest featuresOfInterest) {
        FeatureOfInterest createDto = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), featuresOfInterest);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();

    }

    @Override
    public Response createLocation(ExpandedLocation location) {
        ServiceSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), location);
        ICriterion criterion = parseFilter(EFilterContext.LOCATIONS);
        ICriterion criterionThing = parseFilter(EFilterContext.THINGS);

        Location createDto = DtoMapper.toLocation(getSession(), application, getMapper(), uriInfo, getExpansions(),
                criterion, snapshot, criterionThing);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();

    }

    @Override
    public Response createObservedProperties(ExpandedObservedProperty observedProperty) {
        ExpandedObservedProperty createDto = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), observedProperty);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();

    }

    @Override
    public Response createSensors(ExpandedSensor sensor) {
        ExpandedSensor createDto = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), sensor);

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();

    }

    @Override
    public Response createThing(ExpandedThing thing) {

        ProviderSnapshot snapshot = getExtraDelegate().create(getSession(), getMapper(), uriInfo,
                requestContext.getMethod(), thing);
        ICriterion criterion = parseFilter(EFilterContext.THINGS);

        Thing createDto = DtoMapper.toThing(getSession(), application, getMapper(), uriInfo, getExpansions(), criterion,
                UtilIds.getThingService(snapshot));

        URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(createDto.id())).build();

        return Response.created(createdUri).entity(createDto).build();
    }

}
