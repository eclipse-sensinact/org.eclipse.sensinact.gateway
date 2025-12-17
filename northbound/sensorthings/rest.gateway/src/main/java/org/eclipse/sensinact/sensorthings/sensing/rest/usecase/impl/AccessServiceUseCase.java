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

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.impl.DtoMapperGet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * use case service to get the service
 */
@Component(service = IAccessServiceUseCase.class, immediate = true)
public class AccessServiceUseCase implements IAccessServiceUseCase {

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    IAccessProviderUseCase accessProviderUserCase;

    @Override
    public ServiceSnapshot read(SensiNactSession session, String providerId, String serviceId) {

        ProviderSnapshot providerSnapshot = validateAndGetProvider(session, providerId);

        String service = DtoMapperGet.extractFirstIdSegment(serviceId);

        ServiceSnapshot serviceSnapshot = providerSnapshot.getService(service);

        return serviceSnapshot;
    }

    private ProviderSnapshot validateAndGetProvider(SensiNactSession session, String providerId) {
        return accessProviderUserCase.read(session, providerId);
    }

}
