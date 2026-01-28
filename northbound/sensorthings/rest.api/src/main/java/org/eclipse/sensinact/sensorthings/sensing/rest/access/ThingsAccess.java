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
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
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
    public Thing getThing(@PathParam("id") String id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Thing getThingProp(@PathParam("id") String id) {
        return getThing(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Thing getThingPropValue(@PathParam("id") String id) {
        return getThing(id);
    }

    @Path("Datastreams")
    @GET
    public ResultList<Datastream> getThingDatastreams(@PathParam("id") String id);

    @Path("Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getThingDatastreamsRef(@PathParam("id") String id) {
        ResultList<Datastream> thingDatastreams = getThingDatastreams(id);
        return new ResultList<>(thingDatastreams.count(), thingDatastreams.nextLink(), thingDatastreams.value());
    }

    @Path("Datastreams({id2})")
    @GET
    public Datastream getThingDatastream(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Datastreams({id2})/{prop}")
    @GET
    @PropFilter
    default public Datastream getThingDatastreamProp(@PathParam("id") String id, @PathParam("id2") String id2) {
        return getThingDatastream(id, id2);
    }

    @Path("Datastreams({id2})/Observations")
    @GET
    public ResultList<Observation> getThingDatastreamObservations(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Datastreams({id2})/ObservedProperty")
    @GET
    public ObservedProperty getThingDatastreamObservedProperty(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Datastreams({id2})/Sensor")
    @GET
    public Sensor getThingDatastreamSensor(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Datastreams({id2})/Thing")
    @GET
    public Thing getThingDatastreamThing(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingHistoricalLocations(@PathParam("id") String id);

    @Path("HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getThingHistoricalLocationsRef(@PathParam("id") String id) {
        ResultList<HistoricalLocation> thingHistoricalLocations = getThingHistoricalLocations(id);
        return new ResultList<>(thingHistoricalLocations.count(), thingHistoricalLocations.nextLink(),
                thingHistoricalLocations.value());
    }

    @Path("HistoricalLocations({id2})")
    @GET
    public HistoricalLocation getThingHistoricalLocation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("HistoricalLocations({id2})/{prop}")
    @GET
    @PropFilter
    default public HistoricalLocation getThingHistoricalLocationProp(@PathParam("id") String id,
            @PathParam("id2") String id2) {
        return getThingHistoricalLocation(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing")
    @GET
    public Thing getThingHistoricalLocationsThing(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("HistoricalLocations({id2})/Locations")
    @GET
    public ResultList<Location> getThingHistoricalLocationLocations(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Locations")
    @GET
    public ResultList<Location> getThingLocations(@PathParam("id") String id);

    @Path("Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getThingLocationsRef(@PathParam("id") String id) {
        ResultList<Location> thingLocations = getThingLocations(id);
        return new ResultList<>(thingLocations.count(), thingLocations.nextLink(), thingLocations.value());
    }

    @Path("Locations({id2})")
    @GET
    public Location getThingLocation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Locations({id2})/{prop}")
    @GET
    @PropFilter
    default public Location getThingLocationProp(@PathParam("id") String id, @PathParam("id2") String id2) {
        return getThingLocation(id, id2);
    }

    @Path("Locations({id2})/Things")
    @GET
    public ResultList<Thing> getThingLocationThings(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Locations({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getThingLocationHistoricalLocations(@PathParam("id") String id,
            @PathParam("id2") String id2);

}
