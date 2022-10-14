package org.eclipse.sensinact.sensorthings.sensing.rest;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
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
@Path("/v1.1/Observations({id})")
public interface ObservationsAccess {
    
    @GET
    public Observation getObservation(@PathParam("id") String id);
    
    @Path("{prop}")
    @GET
    @PropFilter
    public Observation getObservationProp(@PathParam("id") String id);

    @Path("{prop}/$value")
    @GET
    @PropFilter
    public Observation getObservationPropValue(@PathParam("id") String id);

    @Path("Datastream")
    @GET
    public Datastream getObservationDatastream(@PathParam("id") String id);

    @Path("Datastream/$ref")
    @GET
    @RefFilter
    public Self getObservationDatastreamRef(@PathParam("id") String id);

    @Path("Datastream/{prop}")
    @GET
    @PropFilter
    public Observation getObservationDatastreamProp(@PathParam("id") String id);

    @Path("Datastream/Observations")
    @GET
    public ResultList<Observation> getObservationDatastreamObservations(@PathParam("id") String id);
    
    @Path("Datastream/ObservedProperty")
    @GET
    public ObservedProperty getObservationDatastreamObservedProperty(@PathParam("id") String id);

    @Path("Datastream/Sensor")
    @GET
    public Sensor getObservationDatastreamSensor(@PathParam("id") String id);
    
    @Path("Datastream/Thing")
    @GET
    public Thing getObservationDatastreamThing(@PathParam("id") String id);
    
    @Path("FeatureOfInterest")
    @GET
    public FeatureOfInterest getObservationFeatureOfInterest(@PathParam("id") String id);
    
    @Path("FeatureOfInterest/$ref")
    @GET
    @RefFilter
    public Self getObservationFeatureOfInterestRef(@PathParam("id") String id);

    @Path("FeatureOfInterest/{prop}")
    @GET
    @PropFilter
    public FeatureOfInterest getObservationFeatureOfInterestProp(@PathParam("id") String id);
    
    @Path("FeatureOfInterest/Observations")
    @GET
    public ResultList<Observation> getObservationFeatureOfInterestObservations(@PathParam("id") String id);
    
}