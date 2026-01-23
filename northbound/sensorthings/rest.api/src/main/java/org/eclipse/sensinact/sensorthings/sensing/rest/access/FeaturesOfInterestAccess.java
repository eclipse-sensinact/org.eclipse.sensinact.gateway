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
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Produces(APPLICATION_JSON)
@Path("/v1.1/FeaturesOfInterest({id})")
public interface FeaturesOfInterestAccess {

    @GET
    public FeatureOfInterest getFeatureOfInterest(@PathParam("id") String id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public FeatureOfInterest getFeatureOfInterestProp(@PathParam("id") String id) {
        return getFeatureOfInterest(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public FeatureOfInterest getFeatureOfInterestPropValue(@PathParam("id") String id) {
        return getFeatureOfInterest(id);
    }

    @Path("Observations")
    @GET
    public ResultList<Observation> getFeatureOfInterestObservations(@PathParam("id") String id);

    @Path("Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getFeatureOfInterestObservationsRef(@PathParam("id") String id) {
        ResultList<Observation> featureOfInterestObservations = getFeatureOfInterestObservations(id);
        return new ResultList<Id>(featureOfInterestObservations.count(), featureOfInterestObservations.nextLink(),
                featureOfInterestObservations.value());
    }

    @Path("Observations({id2})")
    @GET
    public Observation getFeatureOfInterestObservation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Observations({id2})/{prop}")
    @GET
    @PropFilter
    default public Observation getFeatureOfInterestObservationsProp(@PathParam("id") String id,
            @PathParam("id2") String id2) {
        return getFeatureOfInterestObservation(id, id2);
    }

    @Path("Observations({id2})/Datastream")
    @GET
    public Datastream getFeatureOfInterestObservationDatastream(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("Observations({id2})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getFeatureOfInterestObservationFeatureOfInterest(@PathParam("id") String id,
            @PathParam("id2") String id2) {
        return getFeatureOfInterest(id);
    }

}
