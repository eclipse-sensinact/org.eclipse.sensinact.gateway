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
    default public Datastream getDatastreamProp(@PathParam("id") String id) {
        return getDatastream(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Datastream getDatastreamPropValue(@PathParam("id") String id) {
        return getDatastream(id);
    }

    @Path("Observations")
    @GET
    public ResultList<Observation> getDatastreamObservations(@PathParam("id") String id);

    @Path("Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getDatastreamObservationsRef(@PathParam("id") String id) {
        ResultList<Observation> datastreamObservations = getDatastreamObservations(id);
        return new ResultList<>(datastreamObservations.count(), datastreamObservations.nextLink(),
                datastreamObservations.value());
    }

    @Path("Observations({id2})")
    @GET
    public Observation getDatastreamObservation(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Observations({id2})/{prop}")
    @GET
    @PropFilter
    default public Observation getDatastreamObservationsProp(@PathParam("id") String id, @PathParam("id2") String id2) {
        return getDatastreamObservation(id, id2);
    }

    @Path("Observations({id2})/Datastream")
    @GET
    public Datastream getDatastreamObservationDatastream(@PathParam("id") String id, @PathParam("id2") String id2);

    @Path("Observations({id2})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(@PathParam("id") String id,
            @PathParam("id2") String id2);

    @Path("ObservedProperty")
    @GET
    public ObservedProperty getDatastreamObservedProperty(@PathParam("id") String id);

    @Path("ObservedProperty/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamObservedPropertyRef(@PathParam("id") String id) {
        return getDatastreamObservedProperty(id);
    }

    @Path("ObservedProperty/{prop}")
    @GET
    @PropFilter
    default public ObservedProperty getDatastreamObservedPropertyProp(@PathParam("id") String id) {
        return getDatastreamObservedProperty(id);
    }

    @Path("ObservedProperty/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(@PathParam("id") String id);

    @Path("Sensor")
    @GET
    public Sensor getDatastreamSensor(@PathParam("id") String id);

    @Path("Sensor/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamSensorRef(@PathParam("id") String id) {
        return getDatastreamSensor(id);
    }

    @Path("Sensor/{prop}")
    @GET
    @PropFilter
    default public Sensor getDatastreamSensorProp(@PathParam("id") String id) {
        return getDatastreamSensor(id);
    }

    @Path("Sensor/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamSensorDatastreams(@PathParam("id") String id);

    @Path("Thing")
    @GET
    public Thing getDatastreamThing(@PathParam("id") String id);

    @Path("Thing/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamThingRef(@PathParam("id") String id) {
        return getDatastreamThing(id);
    }

    @Path("Thing/{prop}")
    @GET
    @PropFilter
    default public Thing getDatastreamThingProp(@PathParam("id") String id) {
        return getDatastreamThing(id);
    }

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
