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

import org.eclipse.sensinact.prototype.ProviderDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.RootResourceAccess;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class RootResourceAccessImpl implements RootResourceAccess {

    @Context
    SensiNactSession userSession;

    @Context
    UriInfo uriInfo;

    @Context
    ObjectMapper mapper;

    @Override
    public ResultList<Thing> getThings() {
        ResultList<Thing> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream().map(p -> DtoMapper.toThing(userSession, uriInfo, p.provider)).collect(toList());

        return list;
    }

    @Override
    public ResultList<Location> getLocations() {
        ResultList<Location> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream().map(p -> DtoMapper.toLocation(userSession, uriInfo, mapper, p.provider)).collect(toList());

        return list;
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ResultList<HistoricalLocation> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream().map(p -> DtoMapper.toHistoricalLocation(userSession, uriInfo, p.provider)).collect(toList());
        return list;
    }

    @Override
    public ResultList<Datastream> getDatastreams() {
        ResultList<Datastream> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .flatMap(p -> p.services.stream().map(s -> userSession.describeService(p.provider, s)))
                .flatMap(s -> s.resources.stream().map(r -> userSession.describeResource(s.provider, s.service, r)))
                .map(r -> DtoMapper.toDatastream(userSession, uriInfo, r)).collect(toList());

        return list;
    }

    @Override
    public ResultList<Sensor> getSensors() {
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
        ResultList<FeatureOfInterest> list = new ResultList<>();

        List<ProviderDescription> providers = userSession.listProviders();
        list.value = providers.stream()
                .map(p -> DtoMapper.toFeatureOfInterest(userSession, uriInfo, mapper, p.provider)).collect(toList());

        return list;
    }

}
