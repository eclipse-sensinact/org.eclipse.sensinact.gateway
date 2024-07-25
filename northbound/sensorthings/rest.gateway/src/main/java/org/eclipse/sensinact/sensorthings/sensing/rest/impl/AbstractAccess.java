/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import static org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings.EMPTY;
import static org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapper.extractFirstIdSegment;

import java.util.Optional;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public abstract class AbstractAccess {

    @Context
    protected UriInfo uriInfo;

    @Context
    protected Providers providers;

    @Context
    protected Application application;

    @Context
    protected ContainerRequestContext requestContext;

    /**
     * Returns a user session
     */
    protected SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns an object mapper
     */
    protected ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }

    protected ExpansionSettings getExpansions() {
        ExpansionSettings es = (ExpansionSettings) requestContext
                .getProperty(IFilterConstants.EXPAND_SETTINGS_STRING);
        return es == null ? EMPTY : es;
    }

    private Optional<ProviderSnapshot> getProviderSnapshot(String id) {
        return getSession().filteredSnapshot(new SnapshotFilter(id)).stream().findFirst();
    }

    protected ProviderSnapshot validateAndGetProvider(String id) {
        DtoMapper.validatedProviderId(id);

        Optional<ProviderSnapshot> providerSnapshot = getProviderSnapshot(id);

        if (providerSnapshot.isEmpty()) {
            throw new NotFoundException("Unknown provider");
        }
        return providerSnapshot.get();
    }

    protected ResourceSnapshot validateAndGetResourceSnapshot(String id) {
        String provider = extractFirstIdSegment(id);

        ProviderSnapshot providerSnapshot = validateAndGetProvider(provider);

        String service = extractFirstIdSegment(id.substring(provider.length() + 1));
        String resource = extractFirstIdSegment(id.substring(provider.length() + service.length() + 2));

        ResourceSnapshot resourceSnapshot = providerSnapshot.getResource(service, resource);

        if(resourceSnapshot == null) {
            throw new NotFoundException();
        }
        return resourceSnapshot;
    }
}
