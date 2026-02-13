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
@Path("/v1.1/Locations({id})")
public interface LocationsAccess {

    @GET
    public Location getLocation(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Location getLocationProp(@PathParam("id") ODataId id) {
        return getLocation(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Location getLocationPropValue(@PathParam("id") ODataId id) {
        return getLocation(id);
    }

    @Path("HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(@PathParam("id") ODataId id);

    @Path("HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getLocationHistoricalLocationsRef(@PathParam("id") ODataId id) {
        ResultList<HistoricalLocation> locationHistoricalLocations = getLocationHistoricalLocations(id);
        return new ResultList<>(locationHistoricalLocations.count(), locationHistoricalLocations.nextLink(),
                locationHistoricalLocations.value());
    }

    @Path("HistoricalLocations({id2})")
    @GET
    public HistoricalLocation getLocationHistoricalLocation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/$ref")
    @GET
    @RefFilter
    default public HistoricalLocation getLocationHistoricalLocationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocation(id, id2);
    }

    @Path("HistoricalLocations({id2})/{prop}")
    @GET
    @PropFilter
    default public HistoricalLocation getLocationHistoricalLocationProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocation(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing")
    @GET
    public Thing getLocationHistoricalLocationsThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getLocationHistoricalLocationsThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocationsThingDatastreams(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing/Locations")
    @GET
    public ResultList<Location> getLocationHistoricalLocationsThingLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getLocationHistoricalLocationsThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocationsThingLocations(id, id2);
    }

    @Path("HistoricalLocations({id2})/Locations({id3})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getLocationHistoricalLocationsLocationHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("HistoricalLocations({id2})/Locations({id3})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getLocationHistoricalLocationsLocationHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationHistoricalLocationsLocationHistoricalLocations(id, id2, id3);
    }

    @Path("HistoricalLocations({id2})/Locations({id3})/Things")
    @GET
    public ResultList<Thing> getLocationHistoricalLocationsLocationThings(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("HistoricalLocations({id2})/Locations({id3})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getLocationHistoricalLocationsLocationThingsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationHistoricalLocationsLocationThings(id, id2, id3);
    }

    @Path("HistoricalLocations({id2})/Thing/Datastreams")
    @GET
    public ResultList<Datastream> getLocationHistoricalLocationsThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getLocationHistoricalLocationsThingHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocationsThingHistoricalLocations(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getLocationHistoricalLocationsThingHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getLocationHistoricalLocationsThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocationsThing(id, id2);
    }

    @Path("HistoricalLocations({id2})/Locations")
    @GET
    public ResultList<Location> getLocationHistoricalLocationLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("HistoricalLocations({id2})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getLocationHistoricalLocationLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocationLocations(id, id2);
    }

    @Path("HistoricalLocations({id2})/Location")
    @GET
    default public Location getLocationHistoricalLocationLocationsLocation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocation(id);
    }

    @Path("HistoricalLocations({id2})/Location/$ref")
    @GET
    @RefFilter
    default public Location getLocationHistoricalLocationLocationsLocationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationHistoricalLocationLocationsLocation(id, id2);
    }

    @Path("Things")
    @GET
    public ResultList<Thing> getLocationThings(@PathParam("id") ODataId id);

    @Path("Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getLocationThingsRef(@PathParam("id") ODataId id) {
        ResultList<Thing> locationThings = getLocationThings(id);
        return new ResultList<>(locationThings.count(), locationThings.nextLink(), locationThings.value());
    }

    @Path("Things({id2}/Location")
    @GET
    default public Location getLocationThingLocation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getLocation(id);
    }

    @Path("Things({id2}/Location/$ref")
    @GET
    @RefFilter
    default public Location getLocationThingLocationRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getLocationThingLocation(id, id2);
    }

    @Path("Things({id2})")
    @GET
    public Thing getLocationThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Things({id2})/$ref")
    @GET
    @RefFilter
    default public Thing getLocationThingRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getLocationThing(id, id2);
    }

    @Path("Things({id2})/HistoricalLocations({id3})")
    @GET
    public Thing getLocationThingHistoricalLocation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Things({id2})/HistoricalLocations({id3})/$ref")
    @GET
    @RefFilter
    default public Thing getLocationThingHistoricalLocationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingHistoricalLocation(id, id2, id3);
    }

    @Path("Things({id2})/{prop}")
    @GET
    @PropFilter
    default public Thing getLocationThingProp(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getLocationThing(id, id2);
    }

    @Path("Things({id2})/Datastreams")
    @GET
    public ResultList<Datastream> getLocationThingDatastreams(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Things({id2})/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getLocationThingDatastreamsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationThingDatastreams(id, id2);
    }

    @Path("Things({id2})/Datastreams({id3})/Thing")
    @GET
    public Thing getLocationThingDatastreamThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Things({id2})/Datastreams({id3})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getLocationThingDatastreamThingRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3) {
        return getLocationThingDatastreamThing(id, id2, id3);
    }

    @Path("Things({id2})/Datastreams({id3})/Sensor")
    @GET
    public Sensor getLocationThingDatastreamSensor(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3);

    @Path("Things({id2})/Datastreams({id3})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getLocationThingDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingDatastreamSensor(id, id2, id3);
    }

    @Path("Things({id2})/Datastreams({id3})/Observations")
    @GET
    public ResultList<Observation> getLocationThingDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Things({id2})/Datastreams({id3})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getLocationThingDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingDatastreamObservations(id, id2, id3);
    }

    @Path("Things({id2})/Datastreams({id3})/Observations")
    @GET
    public FeatureOfInterest getLocationThingDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3, @PathParam("id3") ODataId id4);

    @Path("Things({id2})/Datastreams({id3})/Observations/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getLocationThingDatastreamObservationFeatureOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3,
            @PathParam("id3") ODataId id4) {
        return getLocationThingDatastreamObservationFeatureOfInterest(id, id2, id3, id4);
    }

    @Path("Things({id2})/Datastreams({id3})/ObservedProperty")
    @GET
    public ObservedProperty getLocationThingDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3);

    @Path("Things({id2})/Datastreams({id3})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getLocationThingDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingDatastreamObservedProperty(id, id2, id3);
    }

    @Path("Things({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Things({id2})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getLocationThingHistoricalLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationThingHistoricalLocations(id, id2);
    }

    @Path("Things({id2})/HistoricalLocations({id3})/Thing")
    @GET
    default public Thing getLocationThingHistoricalLocationThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThing(id, id2);
    }

    @Path("Things({id2})/HistoricalLocations({id3})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getLocationThingHistoricalLocationThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingHistoricalLocationThing(id, id2, id3);
    }

    @Path("Things({id2})/HistoricalLocations({id3})/Locations")
    @GET
    default public ResultList<Location> getLocationThingHistoricalLocationLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingLocations(id, id2);
    }

    @Path("Things({id2})/HistoricalLocations({id3})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getLocationThingHistoricalLocationLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingHistoricalLocationLocations(id, id2, id3);
    }

    @Path("Things({id2})/Locations")
    @GET
    public ResultList<Location> getLocationThingLocations(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Things({id2})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getLocationThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getLocationThingLocations(id, id2);
    }

    @Path("Things({id2})/Locations({id3})/HistoricalLocations")
    @GET
    default public ResultList<HistoricalLocation> getLocationThingLocationHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingHistoricalLocations(id, id2);
    }

    @Path("Things({id2})/Locations({id3})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getLocationThingLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingLocationHistoricalLocations(id, id2, id3);
    }

    @Path("Things({id2})/Locations({id3})/Things")
    @GET
    default public ResultList<Thing> getLocationThingLocationThings(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThings(id);
    }

    @Path("Things({id2})/Locations({id3})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getLocationThingLocationThingsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getLocationThingLocationThings(id, id2, id3);
    }
}
