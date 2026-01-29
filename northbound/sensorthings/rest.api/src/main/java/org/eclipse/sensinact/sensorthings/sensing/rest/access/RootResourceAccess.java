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

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.NameUrl;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.ServerSettings;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

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
        ServerSettings serverSettings = new ServerSettings(
                List.of("http://www.opengis.net/spec/iot_sensing/1.1/req/datamodel",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/resource-path",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/order",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/select",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/status-code",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/query-status-code",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/orderby",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/top",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/skip",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/pagination",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/count",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/filter",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/built-in-filter-operations",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/built-in-query-functions",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/expand",
                        "http://www.opengis.net/spec/iot_sensing/1.1/req/request-data/expand-deep"));

        List<NameUrl> value = List.of(new NameUrl("Things", info.getAbsolutePathBuilder().path("Things").toString()),
                new NameUrl("Locations", info.getAbsolutePathBuilder().path("Locations").toString()),
                new NameUrl("HistoricalLocations",
                        info.getAbsolutePathBuilder().path("HistoricalLocations").toString()),
                new NameUrl("Datastreams", info.getAbsolutePathBuilder().path("Datastreams").toString()),
                new NameUrl("Sensors", info.getAbsolutePathBuilder().path("Sensors").toString()),
                new NameUrl("Observations", info.getAbsolutePathBuilder().path("Observations").toString()),
                new NameUrl("ObservedProperties", info.getAbsolutePathBuilder().path("ObservedProperties").toString()),
                new NameUrl("FeaturesOfInterest", info.getAbsolutePathBuilder().path("FeaturesOfInterest").toString()));

        return new RootResponse(serverSettings, value);
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

    @GET
    @Path("Things/$ref")
    @RefFilter
    default ResultList<Thing> getThingsRef() {
        return getThings();
    }

    @GET
    @Path("Locations/$ref")
    @RefFilter
    default ResultList<Location> getLocationsRef() {
        return getLocations();
    }

    @GET
    @Path("HistoricalLocations/$ref")
    @RefFilter
    default ResultList<HistoricalLocation> getHistoricalLocationsRef() {
        return getHistoricalLocations();
    }

    @GET
    @Path("Datastreams/$ref")
    @RefFilter
    default ResultList<Datastream> getDatastreamsRef() {
        return getDatastreams();
    }

    @GET
    @Path("Sensors/$ref")
    @RefFilter
    default ResultList<Sensor> getSensorsRef() {
        return getSensors();
    }

    @GET
    @Path("Observations/$ref")
    @RefFilter
    default ResultList<Observation> getObservationsRef() {
        return getObservations();
    }

    @GET
    @Path("ObservedProperties/$ref")
    @RefFilter
    default ResultList<ObservedProperty> getObservedPropertiesRef() {
        return getObservedProperties();
    }

    @GET
    @Path("FeaturesOfInterest/$ref")
    @RefFilter
    default ResultList<FeatureOfInterest> getFeaturesOfInterestRef() {
        return getFeaturesOfInterest();
    }

}
