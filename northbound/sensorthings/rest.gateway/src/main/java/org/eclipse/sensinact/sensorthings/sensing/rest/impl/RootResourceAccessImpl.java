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

import java.util.List;
import java.util.stream.Stream;

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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.RootResourceAccess;
import org.eclipse.sensinact.sensorthings.sensing.rest.create.RootResourceCreate;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensinact.RootResourceDelegateSensinact;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.sensorthings.RootResourceDelegateSensorthings;

import jakarta.ws.rs.core.Response;

public class RootResourceAccessImpl extends AbstractAccess implements RootResourceAccess, RootResourceCreate {
    private RootResourceDelegateSensinact sensinactHandler;
    private RootResourceDelegateSensorthings sensorthigHandler;

    public RootResourceDelegateSensinact getSensinactHandler() {
        if (sensinactHandler == null)
            sensinactHandler = new RootResourceDelegateSensinact(uriInfo, providers, application, requestContext);
        return sensinactHandler;

    }

    public RootResourceDelegateSensorthings getSensorthingsHandler() {
        if (sensorthigHandler == null)
            sensorthigHandler = new RootResourceDelegateSensorthings(uriInfo, providers, application, requestContext);
        return sensorthigHandler;

    }

    @Override
    public Response createDatastream(ExpandedDataStream datastream) {
        return getSensorthingsHandler().createDatastream(datastream);

    }

    @Override
    public Response createFeaturesOfInterest(FeatureOfInterest featuresOfInterest) {
        return getSensorthingsHandler().createFeaturesOfInterest(featuresOfInterest);

    }

    @Override
    public Response createLocation(ExpandedLocation location) {
        return getSensorthingsHandler().createLocation(location);

    }

    @Override
    public Response createObservedProperties(ObservedProperty observedProperty) {
        return getSensorthingsHandler().createObservedProperties(observedProperty);

    }

    @Override
    public Response createSensors(Sensor sensor) {
        return getSensorthingsHandler().createSensors(sensor);

    }

    @Override
    public Response createThing(ExpandedThing thing) {
        return getSensorthingsHandler().createThing(thing);

    }

    @Override
    public ResultList<Thing> getThings() {
        ResultList<Thing> resultSensinact = getSensinactHandler().getThings();
        ResultList<Thing> resultSensorthing = getSensorthingsHandler().getThings();
        return new ResultList<Thing>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<Location> getLocations() {
        ResultList<Location> resultSensinact = getSensinactHandler().getLocations();
        ResultList<Location> resultSensorthing = getSensorthingsHandler().getLocations();
        return new ResultList<Location>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<HistoricalLocation> getHistoricalLocations() {
        ResultList<HistoricalLocation> resultSensinact = getSensinactHandler().getHistoricalLocations();
        ResultList<HistoricalLocation> resultSensorthing = getSensorthingsHandler().getHistoricalLocations();
        return new ResultList<HistoricalLocation>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<Datastream> getDatastreams() {
        ResultList<Datastream> resultSensinact = getSensinactHandler().getDatastreams();
        ResultList<Datastream> resultSensorthing = getSensorthingsHandler().getDatastreams();
        return new ResultList<Datastream>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<Sensor> getSensors() {
        ResultList<Sensor> resultSensinact = getSensinactHandler().getSensors();
        ResultList<Sensor> resultSensorthing = getSensorthingsHandler().getSensors();
        return new ResultList<Sensor>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<Observation> getObservations() {
        ResultList<Observation> resultSensinact = getSensinactHandler().getObservations();
        ResultList<Observation> resultSensorthing = getSensorthingsHandler().getObservations();
        return new ResultList<Observation>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<ObservedProperty> getObservedProperties() {
        ResultList<ObservedProperty> resultSensinact = getSensinactHandler().getObservedProperties();
        ResultList<ObservedProperty> resultSensorthing = getSensorthingsHandler().getObservedProperties();
        return new ResultList<ObservedProperty>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {
        ResultList<FeatureOfInterest> resultSensinact = getSensinactHandler().getFeaturesOfInterest();
        ResultList<FeatureOfInterest> resultSensorthing = getSensorthingsHandler().getFeaturesOfInterest();
        return new ResultList<FeatureOfInterest>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

    @Override
    public ResultList<RefId> getThingsRef() {
        List<RefId> resultSensinact = getSensinactHandler().getThings().value().stream().map(l -> new RefId(l.id()))
                .toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getThings().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getLocationsRef() {
        List<RefId> resultSensinact = getSensinactHandler().getLocations().value().stream().map(l -> new RefId(l.id()))
                .toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getLocations().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getHistoricalLocationsRef() {
        List<RefId> resultSensinact = getSensinactHandler().getHistoricalLocations().value().stream()
                .map(l -> new RefId(l.id())).toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getHistoricalLocations().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getDatastreamsRef() {
        List<RefId> resultSensinact = getSensinactHandler().getDatastreams().value().stream()
                .map(l -> new RefId(l.id())).toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getDatastreams().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getSensorsRef() {
        List<RefId> resultSensinact = getSensinactHandler().getSensors().value().stream().map(l -> new RefId(l.id()))
                .toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getSensors().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getObservationsRef() {
        List<RefId> resultSensinact = getSensinactHandler().getObservations().value().stream()
                .map(l -> new RefId(l.id())).toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getObservations().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getObservedPropertiesRef() {
        List<RefId> resultSensinact = getSensinactHandler().getObservedProperties().value().stream()
                .map(l -> new RefId(l.id())).toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getObservedProperties().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

    @Override
    public ResultList<RefId> getFeaturesOfInterestRef() {
        List<RefId> resultSensinact = getSensinactHandler().getFeaturesOfInterest().value().stream()
                .map(l -> new RefId(l.id())).toList();
        List<RefId> resultSensorthing = getSensorthingsHandler().getFeaturesOfInterest().value().stream()
                .map(l -> new RefId(l.id())).toList();
        return new ResultList<RefId>(null, null,
                Stream.concat(resultSensinact.stream(), resultSensorthing.stream()).toList());

    }

}
