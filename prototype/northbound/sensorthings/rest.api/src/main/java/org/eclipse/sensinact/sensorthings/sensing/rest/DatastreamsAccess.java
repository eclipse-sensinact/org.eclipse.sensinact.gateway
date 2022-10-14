package org.eclipse.sensinact.sensorthings.sensing.rest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
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
    public Datastream getDatastream(@PathParam("id") String id);
    
    @Path("{prop}")
    @GET
    @PropFilter
    public Datastream getDatastreamProp(@PathParam("id") String id);

    @Path("{prop}/$value")
    @GET
    @PropFilter
    public Datastream getDatastreamPropValue(@PathParam("id") String id);

    @Path("Observations")
    @GET
    public ResultList<Observation> getDatastreamObservations(@PathParam("id") String id);

    @Path("Observations/$ref")
    @GET
    @RefFilter
    public ResultList<Self> getDatastreamObservationsRef(@PathParam("id") String id);

    @Path("Observations({id2})")
    @GET
    public Observation getDatastreamObservation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Observations({id2})/{prop}")
    @GET
    @PropFilter
    public Observation getDatastreamObservationsProp(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Observations({id2})/Datastream")
    @GET
    public Datastream getDatastreamObservationDatastream(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Observations({id2})/FeatureOfInterest")
    @GET
    public Datastream getDatastreamObservationFeatureOfInterest(@PathParam("id") String id, @PathParam("id2") String id2);
    
    @Path("ObservedProperty")
    @GET
    public ObservedProperty getDatastreamObservedProperty(@PathParam("id") String id);
    
    @Path("ObservedProperty/$ref")
    @GET
    @RefFilter
    public Self getDatastreamObservedPropertyRef(@PathParam("id") String id);

    @Path("ObservedProperty/{prop}")
    @GET
    @PropFilter
    public ObservedProperty getDatastreamObservedPropertyProp(@PathParam("id") String id);

    @Path("ObservedProperty/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(@PathParam("id") String id);

    @Path("Sensor")
    @GET
    public Sensor getDatastreamSensor(@PathParam("id") String id);
    
    @Path("Sensor/$ref")
    @GET
    @RefFilter
    public Self getDatastreamSensorRef(@PathParam("id") String id);

    @Path("Sensor/{prop}")
    @GET
    @PropFilter
    public Sensor getDatastreamSensorProp(@PathParam("id") String id);
    
    @Path("Sensor/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamSensorDatastreams(@PathParam("id") String id);

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
    public ResultList<Datastream> getDatastreamThingHistoricalLocations(@PathParam("id") String id);

    @Path("Thing/Locations")
    @GET
    public ResultList<Location> getDatastreamThingLocations(@PathParam("id") String id);
    
}