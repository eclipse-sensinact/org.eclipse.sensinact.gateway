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
@Path("/v1.1/FeaturesOfInterest({id})")
public interface FeaturesOfInterestAccess {

    @GET
    public FeatureOfInterest getFeatureOfInterest(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public FeatureOfInterest getFeatureOfInterestProp(@PathParam("id") ODataId id) {
        return getFeatureOfInterest(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public FeatureOfInterest getFeatureOfInterestPropValue(@PathParam("id") ODataId id) {
        return getFeatureOfInterest(id);
    }

    @Path("Observations")
    @GET
    public ResultList<Observation> getFeatureOfInterestObservations(@PathParam("id") ODataId id);

    @Path("Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getFeatureOfInterestObservationsRef(@PathParam("id") ODataId id) {
        ResultList<Observation> featureOfInterestObservations = getFeatureOfInterestObservations(id);
        return new ResultList<Id>(featureOfInterestObservations.count(), featureOfInterestObservations.nextLink(),
                featureOfInterestObservations.value());
    }

    @Path("Observations({id2})")
    @GET
    public Observation getFeatureOfInterestObservation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/$ref")
    @GET
    @RefFilter
    default public Observation getFeatureOfInterestObservationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservation(id, id2);
    }

    @Path("Observations({id2})/{prop}")
    @GET
    @PropFilter
    default public Observation getFeatureOfInterestObservationsProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservation(id, id2);
    }

    @Path("Observations({id2})/Datastream")
    @GET
    public Datastream getFeatureOfInterestObservationDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getFeatureOfInterestObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastream(id, id2);
    }

    @Path("Observations({id2})/Datastream/Thing")
    @GET
    public Thing getFeatureOfInterestObservationDatastreamThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getFeatureOfInterestObservationDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastreamThing(id, id2);
    }

    @Path("Observations({id2})/Datastream/Observations")
    @GET
    public ResultList<Observation> getFeatureOfInterestObservationDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getFeatureOfInterestObservationDatastreamObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastreamObservations(id, id2);
    }

    @Path("Observations({id2})/FeatureOfInterest/Observations")
    @GET
    default public ResultList<Observation> getFeatureOfInterestObservationFeatureOfInterestObserations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservations(id2);
    }

    @Path("Observations({id2})/FeatureOfInterest/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getFeatureOfInterestObservationFeatureOfInterestObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationFeatureOfInterestObserations(id, id2);
    }

    @Path("Observations({id2})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getFeatureOfInterestObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterest(id);
    }

    @Path("Observations({id2})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getFeatureOfInterestObservationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationFeatureOfInterest(id, id2);
    }

    @Path("Observations({id2})/Datastream/Sensor")
    @GET
    public Sensor getFeatureOfInterestObservationDatastreamSensor(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getFeatureOfInterestObservationDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastreamSensor(id, id2);
    }

    @Path("Observations({id2})/Datastream/ObservedProperty")
    @GET
    public ObservedProperty getFeatureOfInterestObservationDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getFeatureOfInterestObservationDatastreamObservedPropertyRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastreamObservedProperty(id, id2);
    }

    @Path("Observations({id2})/Datastream/ObservedProperty/Datastreams")
    @GET
    default public Datastream getFeatureOfInterestObservationDatastreamObservedPropertyDatastream(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastream(id, id2);
    }

    @Path("Observations({id2})/Datastream/ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public Datastream getFeatureOfInterestObservationDatastreamObservedPropertyDatastreamRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastreamObservedPropertyDatastream(id, id2);
    }

    @Path("Observations({id2})/Datastream/Sensor/Datastreams")
    @GET
    default public Datastream getFeatureOfInterestObservationDatastreamSensorDatastream(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastream(id, id2);

    }

    @Path("Observations({id2})/Datastream/Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public Datastream getFeatureOfInterestObservationDatastreamSensorDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getFeatureOfInterestObservationDatastreamSensorDatastream(id, id2);
    }

}
