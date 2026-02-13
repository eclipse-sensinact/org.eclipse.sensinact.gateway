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
@Path("/v1.1/Things({id})")
public interface ThingsAccess {

    @GET
    public Thing getThing(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Thing getThingProp(@PathParam("id") ODataId id) {
        return getThing(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Thing getThingPropValue(@PathParam("id") ODataId id) {
        return getThing(id);
    }

    @Path("Datastreams")
    @GET
    public ResultList<Datastream> getThingDatastreams(@PathParam("id") ODataId id);

    @Path("Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getThingDatastreamsRef(@PathParam("id") ODataId id) {
        ResultList<Datastream> thingDatastreams = getThingDatastreams(id);
        return new ResultList<>(thingDatastreams.count(), thingDatastreams.nextLink(), thingDatastreams.value());
    }

    @Path("Datastreams({id2})")
    @GET
    public Datastream getThingDatastream(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/{prop}")
    @GET
    @PropFilter
    default public Datastream getThingDatastreamProp(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations")
    @GET
    public ResultList<Observation> getThingDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Observations({id3})")
    @GET
    public Observation getThingDatastreamObservation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getThingDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getThingDatastreamObservationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingDatastreamObservationFeatureOfInterest(id, id2, id3);
    }

    @Path("Datastreams({id2})/Observations({id3})/Datastream")
    @GET
    public Datastream getThingDatastreamObservationDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Datastreams({id2})/Observations({id3})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getThingDatastreamObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingDatastreamObservationDatastream(id, id2, id3);
    }

    @Path("Datastreams({id2})/ObservedProperty")
    @GET
    public ObservedProperty getThingDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Sensor")
    @GET
    public Sensor getThingDatastreamSensor(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Sensor/Datastreams")
    @GET
    public ResultList<Datastream> getThingDatastreamSensorDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getThingDatastreamSensorDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingDatastreamSensorDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/ObservedProperty/Datastreams")
    @GET
    public ResultList<Datastream> getThingDatastreamObservedPropertyDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getThingDatastreamObservedPropertyDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingDatastreamObservedPropertyDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getThingDatastreamSensorRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingDatastreamSensor(id, id2);
    }

    @Path("Datastreams({id2})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getThingDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingDatastreamObservations(id, id2);
    }

    @Path("Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getThingDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingDatastreamObservedProperty(id, id2);
    }

    @Path("Datastreams({id2})/Thing")
    @GET
    public Thing getThingDatastreamThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingDatastreamThingHistoricalLocation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getThingDatastreamThingHistoricalLocationRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingDatastreamThingHistoricalLocation(id, id2);
    }

    @Path("Datastreams({id2})/Thing/Locations")
    @GET
    public ResultList<Location> getThingDatastreamThingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getThingDatastreamThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingDatastreamThingLocations(id, id2);
    }

    @Path("Datastreams({id2})/Thing/Datastreams")
    @GET
    public ResultList<Datastream> getThingDatastreamThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Datastreams({id2})/Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getThingDatastreamThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingDatastreamThingDatastreams(id, id2);
    }

    @Path("Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getThingDatastreamThingRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingDatastreamThing(id, id2);
    }

    @Path("HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingHistoricalLocations(@PathParam("id") ODataId id);

    @Path("HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getThingHistoricalLocationsRef(@PathParam("id") ODataId id) {
        ResultList<HistoricalLocation> thingHistoricalLocations = getThingHistoricalLocations(id);
        return new ResultList<>(thingHistoricalLocations.count(), thingHistoricalLocations.nextLink(),
                thingHistoricalLocations.value());
    }

    @Path("HistoricalLocations({id2})")
    @GET
    public HistoricalLocation getThingHistoricalLocation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/{prop}")
    @GET
    @PropFilter
    default public HistoricalLocation getThingHistoricalLocationProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingHistoricalLocation(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing")
    @GET
    public Thing getThingHistoricalLocationsThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getThingHistoricalLocationsThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingHistoricalLocationsThing(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing/Datastreams")
    @GET
    public ResultList<Datastream> getThingHistoricalLocationsThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingHistoricalLocationsThingHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getThingHistoricalLocationsThingHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingHistoricalLocationsThingHistoricalLocations(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing/Locations")
    @GET
    public ResultList<Location> getThingHistoricalLocationsThingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getThingHistoricalLocationsThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingHistoricalLocationsThingLocations(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getThingHistoricalLocationsThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingHistoricalLocationsThingDatastreams(id, id2);
    }

    @Path("HistoricalLocations({id2})/Locations")
    @GET
    public ResultList<Location> getThingHistoricalLocationLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getThingHistoricalLocationLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingHistoricalLocationLocations(id, id2);
    }

    @Path("HistoricalLocations({id2})/Locations({id3})/Things")
    @GET
    public ResultList<Thing> getThingHistoricalLocationLocationThings(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("HistoricalLocations({id2})/Locations({id3})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getThingHistoricalLocationLocationThingsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingHistoricalLocationLocationThings(id, id2, id3);
    }

    @Path("HistoricalLocations({id2})/Locations({id3})/HistoricalLocations")
    @GET
    public ResultList<Location> getThingHistoricalLocationLocationHistoricalLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("HistoricalLocations({id2})/Locations({id3})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getThingHistoricalLocationLocationHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingHistoricalLocationLocationHistoricalLocations(id, id2, id3);
    }

    @Path("Locations")
    @GET
    public ResultList<Location> getThingLocations(@PathParam("id") ODataId id);

    @Path("Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getThingLocationsRef(@PathParam("id") ODataId id) {
        ResultList<Location> thingLocations = getThingLocations(id);
        return new ResultList<>(thingLocations.count(), thingLocations.nextLink(), thingLocations.value());
    }

    @Path("Locations({id2})")
    @GET
    public Location getThingLocation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Locations({id2})/{prop}")
    @GET
    @PropFilter
    default public Location getThingLocationProp(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingLocation(id, id2);
    }

    @Path("Locations({id2})/Things")
    @GET
    public ResultList<Thing> getThingLocationThings(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Locations({id2})/Thing")
    @GET
    default public Thing getThingLocationThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThing(id);
    }

    @Path("Locations({id2})/Things({id3})/Datastreams")
    @GET
    public ResultList<Datastream> getThingLocationThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Locations({id2})/Things({id3})/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getThingLocationThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingLocationThingDatastreams(id, id2, id3);
    }

    @Path("Locations({id2})/Things({id3})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingLocationThingHistoricalLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Locations({id2})/Things({id3})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getThingLocationThingHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingLocationThingHistoricalLocations(id, id2, id3);
    }

    @Path("Locations({id2})/Things({id3})/Locations")
    @GET
    public ResultList<Location> getThingLocationThingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Locations({id2})/Things({id3})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getThingLocationThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingLocationThingLocations(id, id2, id3);
    }

    @Path("Locations({id2})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getThingLocationThingsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingLocationThings(id, id2);
    }

    @Path("Locations({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getThingLocationThingRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getThingLocationThing(id, id2);
    }

    @Path("Locations({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Locations({id2})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getThingLocationHistoricalLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getThingLocationHistoricalLocations(id, id2);
    }

    @Path("Locations({id2})/HistoricalLocations({id3})/Thing")
    @GET
    public Thing getThingLocationHistoricalLocationThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Locations({id2})/HistoricalLocations({id3})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getThingLocationHistoricalLocationThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingLocationHistoricalLocationThing(id, id2, id3);
    }

    @Path("Locations({id2})/HistoricalLocations({id3})/Locations")
    @GET
    public ResultList<Location> getThingLocationHistoricalLocationLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Locations({id2})/HistoricalLocations({id3})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getThingLocationHistoricalLocationLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getThingLocationHistoricalLocationLocations(id, id2, id3);
    }
}
