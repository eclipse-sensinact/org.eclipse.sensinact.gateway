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
package org.eclipse.sensinact.northbound.rest.api;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.dto.query.WrappedAccessMethodCallParametersDTO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEventSink;

@Produces(APPLICATION_JSON)
@Path("")
public interface IRestNorthbound {

    @Path("")
    @GET
    AbstractResultDTO describeProviders();

    @Path("providers")
    @GET
    AbstractResultDTO listProviders();

    @Path("providers/{providerId}")
    @GET
    AbstractResultDTO describeProvider(@PathParam("providerId") String providerId);

    @Path("providers/{providerId}/services")
    @GET
    AbstractResultDTO listServices(@PathParam("providerId") String providerId);

    @Path("providers/{providerId}/services/{serviceName}")
    @GET
    AbstractResultDTO describeService(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName);

    @Path("providers/{providerId}/services/{serviceName}/resources")
    @GET
    AbstractResultDTO listResources(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}")
    @GET
    AbstractResultDTO describeResource(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/GET")
    @GET
    AbstractResultDTO resourceGet(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/SET")
    @POST
    AbstractResultDTO resourceSet(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName,
            WrappedAccessMethodCallParametersDTO parameters);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/ACT")
    @POST
    AbstractResultDTO resourceAct(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName,
            WrappedAccessMethodCallParametersDTO parameters);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/SUBSCRIBE")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    void watchResource(@PathParam("providerId") String providerId, @PathParam("serviceName") String serviceName,
            @PathParam("rcName") String rcName, @Context SseEventSink eventSink);
}
