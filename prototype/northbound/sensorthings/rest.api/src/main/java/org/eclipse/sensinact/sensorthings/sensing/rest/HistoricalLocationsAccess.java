package org.eclipse.sensinact.sensorthings.sensing.rest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
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
    public HistoricalLocation getHistoricalLocationProp(@PathParam("id") String id);

    @Path("{prop}/$value")
    @GET
    @PropFilter
    public HistoricalLocation getHistoricalLocationPropValue(@PathParam("id") String id);

    @Path("Locations")
    @GET
    public ResultList<Location> getHistoricalLocationLocations(@PathParam("id") String id);

    @Path("Locations/$ref")
    @GET
    @RefFilter
    public ResultList<Self> getHistoricalLocationLocationsRef(@PathParam("id") String id);

    @Path("Locations({id2})")
    @GET
    public Location getHistoricalLocationLocation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Locations({id2})/{prop}")
    @GET
    @PropFilter
    public Location getHistoricalLocationLocationProp(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Locations({id2})/Things")
    @GET
    public ResultList<Thing> getHistoricalLocationLocationThings(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Locations({id2})/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getHistoricalLocationLocationHistoricalLocations(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Thing")
    @GET
    public Thing getDatastreamThing(@PathParam("id") String id);

    @Path("Thing/$ref")
    @GET
    @RefFilter
    public Self getDatastreamThingRef(@PathParam("id") String id);
    
    @Path("Thing/{prop}")
    @GET
    @PropFilter
    public Thing getDatastreamThingProp(@PathParam("id") String id);
    
    @Path("Thing/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamThingDatastreams(@PathParam("id") String id);

    @Path("Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(@PathParam("id") String id);

    @Path("Thing/Locations")
    @GET
    public ResultList<Location> getDatastreamThingLocations(@PathParam("id") String id);
    
}