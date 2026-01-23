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
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
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
    public HistoricalLocation getHistoricalLocation(@PathParam("id") String id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public HistoricalLocation getHistoricalLocationProp(@PathParam("id") String id) {
        return getHistoricalLocation(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public HistoricalLocation getHistoricalLocationPropValue(@PathParam("id") String id) {
        return getHistoricalLocation(id);
    }

    @Path("Locations")
    @GET
    public ResultList<Location> getHistoricalLocationLocations(@PathParam("id") String id);

    @Path("Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getHistoricalLocationLocationsRef(@PathParam("id") String id) {
        ResultList<Location> historicalLocationLocations = getHistoricalLocationLocations(id);
        return new ResultList<>(historicalLocationLocations.count(), historicalLocationLocations.nextLink(),
                historicalLocationLocations.value());
    }

    @Path("Locations({id2})")
    @GET
    public Location getHistoricalLocationLocation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Locations({id2})/{prop}")
    @GET
    @PropFilter
    default public Location getHistoricalLocationLocationProp(@PathParam("id") String id,
            @PathParam("id2") String id2) {
        return getHistoricalLocationLocation(id, id2);
    }

    @Path("Locations({id2})/Things")
    @GET
    public ResultList<Thing> getHistoricalLocationLocationThings(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Locations({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Thing")
    @GET
    public Thing getHistoricalLocationThing(@PathParam("id") String id);

    @Path("Thing/$ref")
    @GET
    @RefFilter
    default public Self getHistoricalLocationThingRef(@PathParam("id") String id) {
        return getHistoricalLocationThingRef(id);
    }

    @Path("Thing/{prop}")
    @GET
    @PropFilter
    default public Thing getHistoricalLocationThingProp(@PathParam("id") String id) {
        return getHistoricalLocationThing(id);
    }

    @Path("Thing/Datastreams")
    @GET
    public ResultList<Datastream> getHistoricalLocationThingDatastreams(@PathParam("id") String id);

    @Path("Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getHistoricalLocationThingHistoricalLocations(@PathParam("id") String id);

    @Path("Thing/Locations")
    @GET
    public ResultList<Location> getHistoricalLocationThingLocations(@PathParam("id") String id);

}
