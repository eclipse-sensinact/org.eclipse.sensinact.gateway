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
package org.eclipse.sensinact.northbound.rest.impl;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;


@JakartarsResource
@Path("/runtime")
@Produces(APPLICATION_JSON)
@PermitAll
public class RestRuntime {

    @Context
    private Providers providers;

    private BundleContext getContext() {
        ContextResolver<BundleContext> resolver = providers.getContextResolver(BundleContext.class, MediaType.WILDCARD_TYPE);
        if (resolver == null) {
            return FrameworkUtil.getBundle(RestRuntime.class).getBundleContext();
        } else {
            return resolver.getContext(BundleContext.class);
        }
    }

    @Path("/bundles")
    @GET
    public List<BundleDTO> getBundles() {

        List<BundleDTO> res = new ArrayList<>();
        for (var bundle: getContext().getBundles()) {
            BundleDTO dto = new BundleDTO();
            dto.name = bundle.getSymbolicName();
            dto.version = bundle.getVersion().toString();
            dto.git_ref = bundle.getHeaders().get("Git-Descriptor");
            dto.git_sha = bundle.getHeaders().get("Git-SHA");
            res.add(dto);
        }
        return res;
    }
}
