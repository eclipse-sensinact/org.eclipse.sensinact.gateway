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
@Path("/v1.1/ObservedProperties({id})")
public interface ObservedPropertiesAccess {

    @GET
    public ObservedProperty getObservedProperty(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public ObservedProperty getObservedPropertyProp(@PathParam("id") ODataId id) {
        return getObservedProperty(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public ObservedProperty getObservedPropertyPropValue(@PathParam("id") ODataId id) {
        return getObservedProperty(id);
    }

    @Path("Datastreams")
    @GET
    public ResultList<Datastream> getObservedPropertyDatastreams(@PathParam("id") ODataId id);

    @Path("Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getObservedPropertyDatastreamsRef(@PathParam("id") ODataId id) {
        ResultList<Datastream> observedPropertyDatastreams = getObservedPropertyDatastreams(id);
        return new ResultList<Id>(observedPropertyDatastreams.count(), observedPropertyDatastreams.nextLink(),
                observedPropertyDatastreams.value());
    }

    @Path("Datastreams({id2})")
    @GET
    public Datastream getObservedPropertyDatastream(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/$ref")
    @GET
    @RefFilter
    default public Datastream getObservedPropertyDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations({id3})")
    @GET
    public Observation getObservedPropertyDatastreamObservation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/$ref")
    @GET
    @RefFilter
    default public Observation getObservedPropertyDatastreamObservationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservedPropertyDatastreamObservation(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/Datastream")
    @GET
    default public Datastream getObservedPropertyDatastreamObservationDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservedPropertyDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations({id3})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getObservedPropertyDatastreamObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservedPropertyDatastreamObservationDatastream(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getObservedPropertyDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getObservedPropertyDatastreamObservationFeaturesOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservedPropertyDatastreamObservationFeatureOfInterest(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/Observations")
    @GET
    public ResultList<Observation> getObservedPropertyDatastreamObservationFeatureOfInterestObservations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getObservedPropertyDatastreamObservationFeaturesOfInterestObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservedPropertyDatastreamObservationFeatureOfInterestObservations(id, id2, id3);
    }

    @Path("Datastreams({id2})/{prop}")
    @GET
    @PropFilter
    default public Datastream getObservedPropertyDatastreamProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations")
    @GET
    public ResultList<Observation> getObservedPropertyDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getObservedPropertyDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamObservations(id, id2);
    }

    @Path("Datastreams({id2})/ObservedProperty")
    @GET
    public ObservedProperty getObservedPropertyDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getObservedPropertyDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamObservedProperty(id, id2);
    }

    @Path("Datastreams({id2})/Sensor")
    @GET
    public Sensor getObservedPropertyDatastreamSensor(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getObservedPropertyDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamSensor(id, id2);
    }

    @Path("Datastreams({id2})/Sensor/Datastreams")
    @GET
    default public ResultList<Datastream> getObservedPropertyDatastreamSensorDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id.value());
        String providerId2 = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerId.equals(providerId2)) {
            throw new NotFoundException();
        }
        return new ResultList<Datastream>(null, null, List.of(getObservedPropertyDatastream(id, id2)));

    }

    @Path("Datastreams({id2})/ObservedProperty/Datastreams")
    @GET
    default public ResultList<Datastream> getObservedPropertyDatastreamObservedPropertyDatastreams(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamSensorDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getObservedPropertyDatastreamObservedPropertyDatastreamsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamObservedPropertyDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getObservedPropertyDatastreamSensorDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamSensorDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Thing")
    @GET
    public Thing getObservedPropertyDatastreamThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getObservedPropertyDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamThing(id, id2);
    }

    @Path("Datastreams({id2})/Thing/Datastreams")
    @GET
    public ResultList<Datastream> getObservedPropertyDatastreamThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getObservedPropertyDatastreamThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamThingDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getObservedPropertyDatastreamThingHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getObservedPropertyDatastreamThingHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamThingHistoricalLocations(id, id2);
    }

    @Path("Datastreams({id2})/Thing/Locations")
    @GET
    public ResultList<Location> getObservedPropertyDatastreamThingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getObservedPropertyDatastreamThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservedPropertyDatastreamThingLocations(id, id2);
    }
}
