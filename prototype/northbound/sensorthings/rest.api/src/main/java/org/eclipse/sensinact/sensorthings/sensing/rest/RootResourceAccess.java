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
import static org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.NameUrl.create;

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.ServerSettings;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

@Produces(APPLICATION_JSON)
@Path("/v1.1")
public interface RootResourceAccess {
    
    @GET
    default RootResponse getRootResponse(@Context UriInfo info) {
        RootResponse response = new RootResponse();
        response.serverSettings = new ServerSettings();
        response.serverSettings.conformance = List.of(
                "http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/order",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/select",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/status-code",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/query-status-code",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/orderby",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/top",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/skip",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/pagination",
                "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/count"
            );

        response.value = List.of(
                create("Things", info.getAbsolutePathBuilder().path("Things").toString()),
                create("Locations", info.getAbsolutePathBuilder().path("Locations").toString()),
                create("HistoricalLocations", info.getAbsolutePathBuilder().path("HistoricalLocations").toString()),
                create("Datastreams", info.getAbsolutePathBuilder().path("Datastreams").toString()),
                create("Sensors", info.getAbsolutePathBuilder().path("Sensors").toString()),
                create("Observations", info.getAbsolutePathBuilder().path("Observations").toString()),
                create("ObservedProperties", info.getAbsolutePathBuilder().path("ObservedProperties").toString()),
                create("FeaturesOfInterest", info.getAbsolutePathBuilder().path("FeaturesOfInterest").toString())
            );
        
        return response;
    }

    @GET
    @Path("Things")
    ResultList<Thing> getThings();

    @GET
    @Path("Locations")
    ResultList<Location> getLocations();

    @GET
    @Path("HistoricalLocations")
    ResultList<HistoricalLocation> getHistoricalLocations();

    @GET
    @Path("Datastreams")
    ResultList<Datastream> getDatastreams();
    
    @GET
    @Path("Sensors")
    ResultList<Sensor> getSensors();

    @GET
    @Path("Observations")
    ResultList<Observation> getObservations();
    
    @GET
    @Path("ObservedProperties")
    ResultList<ObservedProperty> getObservedProperties();
    
    @GET
    @Path("FeaturesOfInterest")
    ResultList<FeatureOfInterest> getFeaturesOfInterest();
}
