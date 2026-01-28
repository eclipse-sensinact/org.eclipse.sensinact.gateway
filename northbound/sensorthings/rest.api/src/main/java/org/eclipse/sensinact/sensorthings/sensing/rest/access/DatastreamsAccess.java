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

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
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
@Path("/v1.1/Datastreams({id})")
public interface DatastreamsAccess {

    @GET
    public Datastream getDatastream(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Datastream getDatastreamProp(@PathParam("id") ODataId id) {
        return getDatastream(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Datastream getDatastreamPropValue(@PathParam("id") ODataId id) {
        return getDatastream(id);
    }

    @Path("Observations")
    @GET
    public ResultList<Observation> getDatastreamObservations(@PathParam("id") ODataId id);

    @Path("Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getDatastreamObservationsRef(@PathParam("id") ODataId id) {
        ResultList<Observation> datastreamObservations = getDatastreamObservations(id);
        return new ResultList<>(datastreamObservations.count(), datastreamObservations.nextLink(),
                datastreamObservations.value());
    }

    @Path("Observations({id2})")
    @GET
    public Observation getDatastreamObservation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/$ref")
    @GET
    @RefFilter
    default public Observation getDatastreamObservationRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getDatastreamObservation(id, id2);

    }

    @Path("Observations({id2})/{prop}")
    @GET
    @PropFilter
    default public Observation getDatastreamObservationsProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservation(id, id2);
    }

    @Path("Observations({id2})/Datastream")
    @GET
    public Datastream getDatastreamObservationDatastream(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getDatastreamObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationDatastream(id, id2);
    }

    @Path("Observations({id2})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getDatastreamObservationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationFeatureOfInterest(id, id2);
    }

    @Path("ObservedProperty")
    @GET
    public ObservedProperty getDatastreamObservedProperty(@PathParam("id") ODataId id);

    @Path("ObservedProperty/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamObservedPropertyRef(@PathParam("id") ODataId id) {
        return getDatastreamObservedProperty(id);
    }

    @Path("ObservedProperty/{prop}")
    @GET
    @PropFilter
    default public ObservedProperty getDatastreamObservedPropertyProp(@PathParam("id") ODataId id) {
        return getDatastreamObservedProperty(id);
    }

    @Path("ObservedProperty/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(@PathParam("id") ODataId id);

    @Path("ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getDatastreamObservedPropertyDatastreamsRef(@PathParam("id") ODataId id) {
        return getDatastreamObservedPropertyDatastreamsRef(id);
    }

    @Path("Sensor")
    @GET
    public Sensor getDatastreamSensor(@PathParam("id") ODataId id);

    @Path("Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getDatastreamSensorRef(@PathParam("id") ODataId id) {
        return getDatastreamSensor(id);
    }

    @Path("Sensor/{prop}")
    @GET
    @PropFilter
    default public Sensor getDatastreamSensorProp(@PathParam("id") ODataId id) {
        return getDatastreamSensor(id);
    }

    @Path("Sensor/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamSensorDatastreams(@PathParam("id") ODataId id);

    @Path("Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getDatastreamSensorDatastreamsRef(@PathParam("id") ODataId id) {
        return getDatastreamSensorDatastreams(id);
    }

    @Path("Thing")
    @GET
    public Thing getDatastreamThing(@PathParam("id") ODataId id);

    @Path("Thing/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamThingRef(@PathParam("id") ODataId id) {
        return getDatastreamThing(id);
    }

    @Path("Thing/Datastreams({id2})/Thing")
    @GET
    default public Thing getDatastreamThingDatastreamThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamThing(id2);
    }

    @Path("Thing/Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getDatastreamThingDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamThing(id, id2);
    }

    @Path("Thing/Datastreams({id2})/Obserations")
    @GET
    default public ResultList<Observation> getDatastreamThingDatastreamObserations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservations(id2);
    }

    @Path("Thing/Datastreams({id2})/Obserations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getDatastreamThingDatastreamObserationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamObserations(id, id2);
    }

    @Path("Thing/Datastreams({id2})/Obserations({id3})")
    @GET
    default public Observation getDatastreamThingDatastreamObservation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservation(id, id3);
    }

    @Path("Thing/Datastreams({id2})/Obserations({id3})/$ref")
    @GET
    @RefFilter
    default public Observation getDatastreamThingDatastreamObserationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getDatastreamThingDatastreamObservation(id, id2, id3);
    }

    @Path("Thing/Datastreams({id2})/Obserations({id3})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getDatastreamThingDatastreamObservationFeatureOfInterest(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservationFeatureOfInterest(id2, id3);
    }

    @Path("Thing/Datastreams({id2})/Obserations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getDatastreamThingDatastreamObserationFeatureOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getDatastreamThingDatastreamObservationFeatureOfInterest(id, id2, id3);
    }

    @Path("Thing/Datastreams({id2})/Sensor")
    @GET
    default public Sensor getDatastreamThingDatastreamSensor(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamSensor(id2);
    }

    @Path("Thing/Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getDatastreamThingDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamSensor(id, id2);
    }

    @Path("Thing/Datastreams({id2})/ObservedProperty")
    @GET
    default public ObservedProperty getDatastreamThingDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservedProperty(id2);
    }

    @Path("Thing/Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getDatastreamThingDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamObservedProperty(id, id2);
    }

    @Path("Thing/{prop}")
    @GET
    @PropFilter
    default public Thing getDatastreamThingProp(@PathParam("id") ODataId id) {
        return getDatastreamThing(id);
    }

    @Path("Thing/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamThingDatastreams(@PathParam("id") ODataId id);

    @Path("Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getDatastreamThingDatastreamsRef(@PathParam("id") ODataId id) {
        return getDatastreamThingDatastreams(id);
    }

    @Path("Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(@PathParam("id") ODataId id);

    @Path("Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocationsRef(
            @PathParam("id") ODataId id) {
        return getDatastreamThingHistoricalLocations(id);
    }

    @Path("Thing/Locations")
    @GET
    public ResultList<Location> getDatastreamThingLocations(@PathParam("id") ODataId id);

    @Path("Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getDatastreamThingLocationsRef(@PathParam("id") ODataId id) {
        return getDatastreamThingLocations(id);
    }

}
