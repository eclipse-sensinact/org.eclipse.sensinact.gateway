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
package org.eclipse.sensinact.gateway.feature.integration.jakartarest.resource;

import org.osgi.service.component.annotations.Component;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

// We can't use @JakartarsResource as tinybundles uses an old version of bnd
@Component(service = Resource.class, property = "osgi.jakartars.resource=true")
@Produces(MediaType.TEXT_PLAIN)
@Path("test")
public class Resource {

    @GET
    public String doGet() {
        return "Hello World";
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public String doPost(String body) {
        return "Hello " + body;
    }

}
