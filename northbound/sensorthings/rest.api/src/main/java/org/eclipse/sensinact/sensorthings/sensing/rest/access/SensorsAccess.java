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
package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Produces(APPLICATION_JSON)
@Path("/v1.1/Sensors({id})")
public interface SensorsAccess {

    @GET
    public Sensor getSensor(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Sensor getSensorProp(@PathParam("id") ODataId id) {
        return getSensor(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Sensor getSensorPropValue(@PathParam("id") ODataId id) {
        return getSensor(id);
    }

    @Path("Datastreams")
    @GET
    public ResultList<Datastream> getSensorDatastreams(@PathParam("id") ODataId id);

    @Path("Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getSensorDatastreamsRef(@PathParam("id") ODataId id) {
        ResultList<Datastream> sensorDatastreams = getSensorDatastreams(id);
        return new ResultList<Id>(sensorDatastreams.count(), sensorDatastreams.nextLink(), sensorDatastreams.value());
    }

    @Path("Datastreams({id2})")
    @GET
    public Datastream getSensorDatastream(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/{prop}")
    @GET
    @PropFilter
    default public Datastream getSensorDatastreamProp(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getSensorDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations")
    @GET
    public ResultList<Observation> getSensorDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getSensorDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamObservations(id, id2);
    }

    @Path("Datastreams({id2})/ObservedProperty")
    @GET
    public ObservedProperty getSensorDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getSensorDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamObservedProperty(id, id2);
    }

    @Path("Datastreams({id2})/Sensor")
    @GET
    public Sensor getSensorDatastreamSensor(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getSensorDatastreamSensorRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getSensorDatastreamSensor(id, id2);
    }

    @Path("Datastreams({id2})/Thing")
    @GET
    public Thing getSensorDatastreamThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getSensorDatastreamThingRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getSensorDatastreamThing(id, id2);
    }

    @Path("Datastreams({id2})/Thing/Datastreams")
    @GET
    public ResultList<Datastream> getSensorDatastreamThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getSensorDatastreamThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamThingDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getSensorDatastreamThingHistoricalLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getSensorDatastreamThingHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getSensorDatastreamThingHistoricalLocations(id, id2);
    }

    @Path("Datastreams({id2})/ObservedProperty/Datastreams")
    @GET
    default public ResultList<Datastream> getSensorDatastreamObservedPropertyDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }
        return new ResultList<Datastream>(null, null, List.of(getSensorDatastream(id, id2)));
    }

    @Path("Datastreams({id2})/Sensor/Datastreams")
    @GET
    default public ResultList<Datastream> getSensorDatastreamSensorDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamObservedPropertyDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getSensorDatastreamSensorDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamSensorDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Observations({id3})")
    @GET
    public Observation getSensorDatastreamObservation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/$ref")
    @GET
    @RefFilter
    default public Observation getSensorDatastreamObservationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getSensorDatastreamObservation(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/Datastream")
    @GET
    default public Datastream getSensorDatastreamObservationDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getSensorDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations({id3})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getSensorDatastreamObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getSensorDatastreamObservationDatastream(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getSensorDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getSensorDatastreamObservationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getSensorDatastreamObservationFeatureOfInterest(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/Observations")
    @GET
    public ResultList<Observation> getSensorDatastreamObservationFeatureOfInterestObservations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getSensorDatastreamObservationFeatureOfInterestObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getSensorDatastreamObservationFeatureOfInterestObservations(id, id2, id3);
    }

    @Path("Datastreams({id2})/ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getSensorDatastreamObservedPropertyDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamObservedPropertyDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Thing/Locations")
    @GET
    public ResultList<Location> getSensorDatastreamThingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getSensorDatastreamThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getSensorDatastreamThingLocations(id, id2);
    }
}
