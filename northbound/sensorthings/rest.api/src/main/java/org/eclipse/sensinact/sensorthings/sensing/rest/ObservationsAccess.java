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
    default public Observation getObservationProp(@PathParam("id") String id) {
        return getObservation(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Observation getObservationPropValue(@PathParam("id") String id) {
        return getObservation(id);
    }

    @Path("Datastream")
    @GET
    public Datastream getObservationDatastream(@PathParam("id") String id);

    @Path("Datastream/$ref")
    @GET
    @RefFilter
    default public Self getObservationDatastreamRef(@PathParam("id") String id) {
        return getObservationDatastream(id);
    }

    @Path("Datastream/{prop}")
    @GET
    @PropFilter
    default public Datastream getObservationDatastreamProp(@PathParam("id") String id) {
        return getObservationDatastream(id);
    }

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
    default public Self getObservationFeatureOfInterestRef(@PathParam("id") String id) {
        return getObservationFeatureOfInterest(id);
    }

    @Path("FeatureOfInterest/{prop}")
    @GET
    @PropFilter
    default public FeatureOfInterest getObservationFeatureOfInterestProp(@PathParam("id") String id) {
        return getObservationFeatureOfInterest(id);
    }

    @Path("FeatureOfInterest/Observations")
    @GET
    public ResultList<Observation> getObservationFeatureOfInterestObservations(@PathParam("id") String id);

}
