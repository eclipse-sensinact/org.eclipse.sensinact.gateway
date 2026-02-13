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
@Path("/v1.1/HistoricalLocations({id})")
public interface HistoricalLocationsAccess {

    @GET
    public HistoricalLocation getHistoricalLocation(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public HistoricalLocation getHistoricalLocationProp(@PathParam("id") ODataId id) {
        return getHistoricalLocation(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public HistoricalLocation getHistoricalLocationPropValue(@PathParam("id") ODataId id) {
        return getHistoricalLocation(id);
    }

    @Path("Locations")
    @GET
    public ResultList<Location> getHistoricalLocationLocations(@PathParam("id") ODataId id);

    @Path("Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getHistoricalLocationLocationsRef(@PathParam("id") ODataId id) {
        ResultList<Location> historicalLocationLocations = getHistoricalLocationLocations(id);
        return new ResultList<>(historicalLocationLocations.count(), historicalLocationLocations.nextLink(),
                historicalLocationLocations.value());
    }

    @Path("Locations({id2})")
    @GET
    public Location getHistoricalLocationLocation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Locations({id2})/$ref")
    @GET
    @RefFilter
    default public Location getHistoricalLocationLocationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocation(id, id2);
    }

    @Path("Locations({id2})/{prop}")
    @GET
    @PropFilter
    default public Location getHistoricalLocationLocationProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocation(id, id2);
    }

    @Path("Locations({id2})/Things")
    @GET
    public ResultList<Thing> getHistoricalLocationLocationThings(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Locations({id2})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getHistoricalLocationLocationThingsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocationThings(id, id2);
    }

    @Path("Locations({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Locations({id2})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocationHistoricalLocations(id, id2);
    }

    @Path("Locations({id2})/HistoricalLocation")
    @GET
    default public HistoricalLocation getHistoricalLocationLocationHistoricalLocation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocation(id);
    }

    @Path("Locations({id2})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public HistoricalLocation getHistoricalLocationLocationHistoricalLocationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocationHistoricalLocation(id, id2);
    }

    @Path("Thing")
    @GET
    public Thing getHistoricalLocationThing(@PathParam("id") ODataId id);

    @Path("Thing/$ref")
    @GET
    @RefFilter
    default public Self getHistoricalLocationThingRef(@PathParam("id") ODataId id) {
        return getHistoricalLocationThing(id);
    }

    @Path("Thing/{prop}")
    @GET
    @PropFilter
    default public Thing getHistoricalLocationThingProp(@PathParam("id") ODataId id) {
        return getHistoricalLocationThing(id);
    }

    @Path("Thing/Datastreams")
    @GET
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(@PathParam("id") ODataId id);

    @Path("Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(@PathParam("id") ODataId id);

    @Path("Thing/Locations")
    @GET
    public ResultList<Location> getHistoricalLocationThingLocations(@PathParam("id") ODataId id);

    @Path("Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getHistoricalLocationThingDatastreamsRef(@PathParam("id") ODataId id) {
        return getHistoricalLocationThingDatastreams(id);
    }

    @Path("Thing/Datastreams({id3})/Thing")
    @GET
    default public Thing getHistoricalLocationThingDatastreamThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationThing(id);
    }

    @Path("Thing/Datastreams({id3})/Sensor")
    @GET
    public Sensor getHistoricalLocationThingDatastreamSensor(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Thing/Datastreams({id3})/Observations")
    @GET
    public ResultList<Observation> getHistoricalLocationThingDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Thing/Datastreams({id3})/Observations({id4})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getHistoricalLocationThingDatastreamObservationFeatureOfInterest(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3,
            @PathParam("i4") ODataId id4);

    @Path("Thing/Datastreams({id3})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getHistoricalLocationThingDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingDatastreamSensor(id, id2, id3);
    }

    @Path("Thing/HistoricalLocations({id3})/Thing")
    @GET
    default public Thing getHistoricalLocationThingHistoricalLocationThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThing(id);
    }

    @Path("Thing/HistoricalLocations({id3})/Locations")
    @GET
    default public ResultList<Location> getHistoricalLocationThingHistoricalLocationLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationLocations(id);
    }

    @Path("Thing/Locations({id2})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getHistoricalLocationThingLocationThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationThingLocationThing(id, id2);
    }

    @Path("Thing/Locations({id2})/Things")
    @GET
    default public ResultList<Thing> getHistoricalLocationThingLocationThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocationThings(id, id2);
    }

    @Path("Thing/Locations({id2})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getHistoricalLocationThingLocationHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getHistoricalLocationThingLocationHistoricalLocations(id, id2);
    }

    @Path("Thing/Locations({id2})/HistoricalLocations")
    @GET
    default public ResultList<HistoricalLocation> getHistoricalLocationThingLocationHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getHistoricalLocationLocationHistoricalLocations(id, id2);
    }

    @Path("Locations({id2})/HistoricalLocations({id3})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getHistoricalLocationThingLocationHistoricalLocationThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingLocationHistoricalLocationThing(id, id2, id3);
    }

    @Path("Locations({id2})/Things({id4})/Datastreams")
    @GET
    default public ResultList<Datastream> getHistoricalLocationThingLocationhingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingDatastreams(id);
    }

    @Path("Locations({id2})/Things({id3})/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getHistoricalLocationThingLocationThingDatastreamsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingLocationhingDatastreams(id, id2, id3);
    }

    @Path("Locations({id2})/Things({id3})/HistoricalLocations")
    @GET
    default public ResultList<HistoricalLocation> getHistoricalLocationThingLocationhingHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationLocationHistoricalLocations(id, id2);
    }

    @Path("Locations({id2})/Things({id3})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getHistoricalLocationThingLocationThingHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingLocationhingHistoricalLocations(id, id2, id3);
    }

    @Path("Locations({id2})/Things({id3})/Locations")
    @GET
    default public ResultList<Location> getHistoricalLocationThingLocationhingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationLocations(id);
    }

    @Path("Locations({id2})/Things({id3})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getHistoricalLocationThingLocationThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingLocationhingLocations(id, id2, id3);
    }

    @Path("Locations({id2})/HistoricalLocations({id3})/Thing")
    @GET
    default public Thing getHistoricalLocationThingLocationHistoricalLocationThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThing(id);
    }

    @Path("Locations({id2})/HistoricalLocations({id3})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getHistoricalLocationThingLocationHistoricalLocationLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingLocationHistoricalLocationLocations(id, id2, id3);
    }

    @Path("Locations({id2})/HistoricalLocations({id3})/Locations")
    @GET
    default public ResultList<Location> getHistoricalLocationThingLocationHistoricalLocationLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationLocations(id);
    }

    @Path("Thing/HistoricalLocations({id3})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getHistoricalLocationThingHistoricalLocationLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingHistoricalLocationLocations(id, id2, id3);
    }

    @Path("Thing/HistoricalLocations({id3})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getHistoricalLocationThingHistoricalLocationThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingHistoricalLocationThing(id, id2, id3);
    }

    @Path("Thing/Datastreams({id3})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getHistoricalLocationThingDatastreamObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingDatastreamObservations(id, id2, id3);
    }

    @Path("Thing/Datastreams({id3})/Observations({id4})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getHistoricalLocationThingDatastreamObservationFeatureOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3,
            @PathParam("id4") ODataId id4) {
        return getHistoricalLocationThingDatastreamObservationFeatureOfInterest(id, id2, id3, id4);
    }

    @Path("Thing/Datastreams({id3})/ObservedProperty")
    @GET

    public ObservedProperty getHistoricalLocationThingDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Thing/Datastreams({id3})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getHistoricalLocationThingDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getHistoricalLocationThingDatastreamObservedProperty(id, id2, id3);
    }

    @Path("Thing/Datastreams({id3})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getHistoricalLocationThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getHistoricalLocationThingDatastreamThing(id, id2);
    }

    @Path("Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocationsRef(
            @PathParam("id") ODataId id) {
        return getHistoricalLocationThingHistoricalLocations(id);
    }

    @Path("Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getHistoricalLocationThingLocationsRef(@PathParam("id") ODataId id) {
        return getHistoricalLocationThingLocations(id);
    }

}
