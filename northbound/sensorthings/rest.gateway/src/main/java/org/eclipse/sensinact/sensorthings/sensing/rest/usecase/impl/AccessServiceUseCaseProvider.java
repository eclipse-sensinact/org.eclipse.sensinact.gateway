/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.usecase.impl;

import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

/**
 * Provides access to Service snapshots via a {@link ContextResolver}
 */
@Provider
public class AccessServiceUseCaseProvider implements ContextResolver<IAccessServiceUseCase>, IAccessServiceUseCase {

    @Context
    Providers providers;

    @Override
    public IAccessServiceUseCase getContext(Class<?> type) {
        return this;
    }

    public ServiceSnapshot read(SensiNactSession session, String id) {
        return read(session, UtilIds.extractFirstIdSegment(id), UtilIds.extractSecondIdSegment(id));
    }

    public ServiceSnapshot read(SensiNactSession session, String providerId, String serviceId) {
        ProviderSnapshot providerSnapshot = validateAndGetProvider(session, providerId);

        ServiceSnapshot serviceSnapshot = providerSnapshot.getService(serviceId);

        return serviceSnapshot;
    }

    /**
     * Get hold of the provider snapshot via a {@link ContextResolver}
     *
     * @param session
     * @param providerId
     * @return
     */
    private ProviderSnapshot validateAndGetProvider(SensiNactSession session, String providerId) {
        ContextResolver<IAccessProviderUseCase> cr = providers.getContextResolver(IAccessProviderUseCase.class,
                MediaType.WILDCARD_TYPE);
        if (cr == null) {
            throw new InternalServerErrorException("Unable to locate the provider access service");
        }
        return cr.getContext(Object.class).read(session, providerId);
    }
}
