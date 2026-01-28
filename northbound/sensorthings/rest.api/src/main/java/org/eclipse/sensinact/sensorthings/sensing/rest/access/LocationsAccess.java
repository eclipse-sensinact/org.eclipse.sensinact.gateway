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
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
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
    public Location getLocation(@PathParam("id") String id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Location getLocationProp(@PathParam("id") String id) {
        return getLocation(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Location getLocationPropValue(@PathParam("id") String id) {
        return getLocation(id);
    }

    @Path("HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getLocationHistoricalLocations(@PathParam("id") String id);

    @Path("HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getLocationHistoricalLocationsRef(@PathParam("id") String id) {
        ResultList<HistoricalLocation> locationHistoricalLocations = getLocationHistoricalLocations(id);
        return new ResultList<>(locationHistoricalLocations.count(), locationHistoricalLocations.nextLink(),
                locationHistoricalLocations.value());
    }

    @Path("HistoricalLocations({id2})")
    @GET
    public HistoricalLocation getLocationHistoricalLocation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("HistoricalLocations({id2})/{prop}")
    @GET
    @PropFilter
    default public HistoricalLocation getLocationHistoricalLocationProp(@PathParam("id") String id,
            @PathParam("id2") String id2) {
        return getLocationHistoricalLocation(id, id2);
    }

    @Path("HistoricalLocations({id2})/Thing")
    @GET
    public Thing getLocationHistoricalLocationsThing(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("HistoricalLocations({id2})/Locations")
    @GET
    public ResultList<Location> getLocationHistoricalLocationLocations(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Things")
    @GET
    public ResultList<Thing> getLocationThings(@PathParam("id") String id);

    @Path("Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getLocationThingsRef(@PathParam("id") String id) {
        ResultList<Thing> locationThings = getLocationThings(id);
        return new ResultList<>(locationThings.count(), locationThings.nextLink(), locationThings.value());
    }

    @Path("Things({id2})")
    @GET
    public Thing getLocationThing(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Things({id2})/{prop}")
    @GET
    @PropFilter
    default public Thing getLocationThingProp(@PathParam("id") String id, @PathParam("id2") String id2) {
        return getLocationThing(id, id2);
    }

    @Path("Things({id2})/Datastreams")
    @GET
    public ResultList<Datastream> getLocationThingDatastreams(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Things({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getLocationThingHistoricalLocations(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Things({id2})/Locations")
    @GET
    public ResultList<Location> getLocationThingLocations(@PathParam("id") String id, @PathParam("id2") String id2);

}
