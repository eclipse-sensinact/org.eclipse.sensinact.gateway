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
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Produces(APPLICATION_JSON)
@Path("/v1.1/Observations({id})")
public interface ObservationsAccess {

    @GET
    public Observation getObservation(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Observation getObservationProp(@PathParam("id") ODataId id) {
        return getObservation(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Observation getObservationPropValue(@PathParam("id") ODataId id) {
        return getObservation(id);
    }

    @Path("Datastream")
    @GET
    public Datastream getObservationDatastream(@PathParam("id") ODataId id);

    @Path("Datastream/$ref")
    @GET
    @RefFilter
    default public Self getObservationDatastreamRef(@PathParam("id") ODataId id) {
        return getObservationDatastream(id);
    }

    @Path("Datastream/{prop}")
    @GET
    @PropFilter
    default public Datastream getObservationDatastreamProp(@PathParam("id") ODataId id) {
        return getObservationDatastream(id);
    }

    @Path("Datastream/Observations")
    @GET
    public ResultList<Observation> getObservationDatastreamObservations(@PathParam("id") ODataId id);

    @Path("Datastream/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getObservationDatastreamObservationsRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamObservations(id);
    }

    @Path("Datastream/ObservedProperty")
    @GET
    public ObservedProperty getObservationDatastreamObservedProperty(@PathParam("id") ODataId id);

    @Path("Datastream/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getObservationDatastreamObservedPropertyRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamObservedProperty(id);
    }

    @Path("Datastream/Sensor")
    @GET
    public Sensor getObservationDatastreamSensor(@PathParam("id") ODataId id);

    @Path("Datastream/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getObservationDatastreamSensorRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamSensor(id);
    }

    @Path("Datastream/Sensor/Datastreams")
    @GET
    default public ResultList<Datastream> getObservationDatastreamSensorDatastreams(@PathParam("id") ODataId id) {
        return new ResultList<Datastream>(null, null, List.of(getObservationDatastream(id)));
    }

    @Path("Datastream/Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getObservationDatastreamSensorDatastreamsRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamSensorDatastreams(id);
    }

    @Path("Datastream/ObservedProperty/Datastreams")
    @GET
    default public ResultList<Datastream> getObservationDatastreamObservedPropertyDatastreams(
            @PathParam("id") ODataId id) {
        return new ResultList<Datastream>(null, null, List.of(getObservationDatastream(id)));
    }

    @Path("Datastream/ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getObservationDatastreamObservedPropertyDatastreamsRef(
            @PathParam("id") ODataId id) {
        return getObservationDatastreamSensorDatastreams(id);
    }

    @Path("Datastream/Observations({id2})/Datastream")
    @GET
    default public Datastream getObservationDatastreamObservationDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationDatastream(id2);
    }

    @Path("Datastream/Observations({id2})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getObservationDatastreamObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationDatastreamObservationDatastream(id, id2);
    }

    @Path("Datastream/Observations({id2})/FeatureOfInterest/Observations({id3})/Datastream")
    @GET
    default public Datastream getObservationDatastreamObservationFeatureOfInterestObservationDatastream(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservationDatastream(id3);
    }

    @Path("Datastream/Observations({id2})/FeatureOfInterest/Observations({id3})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getObservationDatastreamObservationFeatureOfInterestObservationDatastreamRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getObservationDatastreamObservationFeatureOfInterestObservationDatastream(id, id2, id3);
    }

    @Path("Datastream/Observations({id2})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getObservationDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationFeatureOfInterest(id2);
    }

    @Path("Datastream/Observations({id2})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getObservationDatastreamObservationFeatureOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getObservationDatastreamObservationFeatureOfInterest(id, id2);
    }

    @Path("Datastream/Thing/Datastreams")
    @GET
    public ResultList<Datastream> getObservationDatastreamThingDataastreams(@PathParam("id") ODataId id);

    @Path("Datastream/Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getObservationDatastreamThingDataastreamsRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamThingDataastreams(id);
    }

    @Path("Datastream/Thing")
    @GET
    public Thing getObservationDatastreamThing(@PathParam("id") ODataId id);

    @Path("Datastream/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getObservationDatastreamThingRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamThing(id);
    }

    @Path("Datastream/Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getObservationDatastreamThingHistoricalLocations(@PathParam("id") ODataId id);

    @Path("Datastream/Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getObservationDatastreamThingHistoricalLocationsRef(
            @PathParam("id") ODataId id) {
        return getObservationDatastreamThingHistoricalLocations(id);
    }

    @Path("Datastream/Thing/Locations")
    @GET
    public ResultList<Location> getObservationDatastreamThingLocations(@PathParam("id") ODataId id);

    @Path("Datastream/Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getObservationDatastreamThingLocationsRef(@PathParam("id") ODataId id) {
        return getObservationDatastreamThingLocations(id);
    }

    @Path("FeatureOfInterest")
    @GET
    public FeatureOfInterest getObservationFeatureOfInterest(@PathParam("id") ODataId id);

    @Path("FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public Self getObservationFeatureOfInterestRef(@PathParam("id") ODataId id) {
        return getObservationFeatureOfInterest(id);
    }

    @Path("FeatureOfInterest/{prop}")
    @GET
    @PropFilter
    default public FeatureOfInterest getObservationFeatureOfInterestProp(@PathParam("id") ODataId id) {
        return getObservationFeatureOfInterest(id);
    }

    @Path("FeatureOfInterest/Observations")
    @GET
    public ResultList<Observation> getObservationFeatureOfInterestObservations(@PathParam("id") ODataId id);

    @Path("FeatureOfInterest/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getObservationFeatureOfInterestObservationsRef(@PathParam("id") ODataId id) {
        return getObservationFeatureOfInterestObservations(id);
    }

    @Path("FeatureOfInterest/Observations({id2})")
    @GET
    default public Observation getObservationFeatureOfInterestObservation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservation(id2);
    }

    @Path("FeatureOfInterest/Observations({id2})/$ref")
    @GET
    @RefFilter
    default public Observation getObservationFeatureOfInterestObservationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationFeatureOfInterestObservation(id, id2);
    }

    @Path("FeatureOfInterest/Observations({id2})/Datastream")
    @GET
    default public Datastream getObservationFeatureOfInterestObservationDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationDatastream(id2);
    }

    @Path("FeatureOfInterest/Observations({id2})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getObservationFeatureOfInterestObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationFeatureOfInterestObservationDatastream(id, id2);
    }

    @Path("FeatureOfInterest/Observations({id2})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getObservationFeatureOfInterestObservationFeatureOfInterest(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getObservationFeatureOfInterest(id2);
    }

    @Path("FeatureOfInterest/Observations({id2})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public Self getObservationFeatureOfInterestObservationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getObservationFeatureOfInterestObservationFeatureOfInterest(id, id2);
    }

}
