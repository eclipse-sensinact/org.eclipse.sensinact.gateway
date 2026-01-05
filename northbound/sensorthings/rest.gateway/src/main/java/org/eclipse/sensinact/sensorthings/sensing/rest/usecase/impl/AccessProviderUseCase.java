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

import java.util.Optional;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.extended.DtoMapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * service that allow to get the provider
 */
@Component(service = IAccessProviderUseCase.class, immediate = true)
public class AccessProviderUseCase implements IAccessProviderUseCase {

    @Reference
    GatewayThread thread;

    @Override
    public ProviderSnapshot read(SensiNactSession session, String providerId) {

        Optional<ProviderSnapshot> providerSnapshot = DtoMapper.getProviderSnapshot(session, providerId);
        if (providerSnapshot.isEmpty()) {
            return null;
        }
        return providerSnapshot.get();
    }

}
