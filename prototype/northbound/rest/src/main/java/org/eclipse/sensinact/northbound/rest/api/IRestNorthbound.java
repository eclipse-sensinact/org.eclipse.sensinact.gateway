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

import java.util.List;

import org.eclipse.sensinact.northbound.rest.dto.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteResourceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteServiceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.GetResponse;
import org.eclipse.sensinact.northbound.rest.dto.ProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultActResponse;
import org.eclipse.sensinact.northbound.rest.dto.ResultCompleteListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultProvidersListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultResourcesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultServicesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultTypedResponseDTO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.sse.SseEventSink;

@Produces(APPLICATION_JSON)
@Path("")
public interface IRestNorthbound {

    @Path("")
    @GET
    ResultCompleteListDTO describeProviders();

    @Path("providers")
    @GET
    ResultProvidersListDTO listProviders();

    @Path("providers/{providerId}")
    @GET
    ResultTypedResponseDTO<ProviderDescriptionDTO> describeProvider(@PathParam("providerId") String providerId);

    @Path("providers/{providerId}/services")
    @GET
    ResultServicesListDTO listServices(@PathParam("providerId") String providerId);

    @Path("providers/{providerId}/services/{serviceName}")
    @GET
    ResultTypedResponseDTO<CompleteServiceDescriptionDTO> describeService(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName);

    @Path("providers/{providerId}/services/{serviceName}/resources")
    @GET
    ResultResourcesListDTO listResources(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}")
    @GET
    ResultTypedResponseDTO<CompleteResourceDescriptionDTO> describeResource(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/GET")
    @GET
    <T> ResultTypedResponseDTO<GetResponse<T>> resourceGet(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/SET")
    @POST
    ResultTypedResponseDTO<GetResponse<?>> resourceSet(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName,
            List<AccessMethodCallParameterDTO> parameters);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/ACT")
    @POST
    ResultActResponse<?> resourceAct(@PathParam("providerId") String providerId,
            @PathParam("serviceName") String serviceName, @PathParam("rcName") String rcName,
            List<AccessMethodCallParameterDTO> parameters);

    @Path("providers/{providerId}/services/{serviceName}/resources/{rcName}/SUBSCRIBE")
    @GET
    void watchResource(@PathParam("providerId") String providerId, @PathParam("serviceName") String serviceName,
            @PathParam("rcName") String rcName, @Context SseEventSink eventSink);
}
