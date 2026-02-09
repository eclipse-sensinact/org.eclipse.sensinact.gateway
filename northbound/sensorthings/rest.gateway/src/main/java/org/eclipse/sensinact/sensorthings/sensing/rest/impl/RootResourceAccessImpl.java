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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedHistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
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
    public Response createObservations(ExpandedObservation observation) {
        return getSensorthingsHandler().createObservation(observation);
    }

    @Override
    public Response createHistoricalLocation(ExpandedHistoricalLocation historicalLocation) {

        return getSensorthingsHandler().createHistoricalLocation(historicalLocation);

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
        Stream<Sensor> list = Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream());
        list = Stream.concat(list, getCacheSensor().values().stream());
        return new ResultList<Sensor>(null, null, list.toList());
    }

    @Override
    public ResultList<Observation> getObservations() {

        ResultList<Observation> resultSensinact = getSensinactHandler().getObservations();
        ResultList<Observation> resultSensorthing = getSensorthingsHandler().getObservations();
        Stream<Observation> result = Stream.concat(resultSensinact.value().stream(),
                resultSensorthing.value().stream());

        return new ResultList<Observation>(null, null, result.toList());
    }

    @Override
    public ResultList<ObservedProperty> getObservedProperties() {

        ResultList<ObservedProperty> resultSensinact = getSensinactHandler().getObservedProperties();
        ResultList<ObservedProperty> resultSensorthing = getSensorthingsHandler().getObservedProperties();
        Stream<ObservedProperty> list = Stream.concat(resultSensinact.value().stream(),
                resultSensorthing.value().stream());
        list = Stream.concat(list, getCacheObservedProperty().values().stream());
        return new ResultList<ObservedProperty>(null, null, list.toList());
    }

    @Override
    public ResultList<FeatureOfInterest> getFeaturesOfInterest() {

        ResultList<FeatureOfInterest> resultSensinact = getSensinactHandler().getFeaturesOfInterest();
        ResultList<FeatureOfInterest> resultSensorthing = getSensorthingsHandler().getFeaturesOfInterest();
        return new ResultList<FeatureOfInterest>(null, null,
                Stream.concat(resultSensinact.value().stream(), resultSensorthing.value().stream()).toList());
    }

}
