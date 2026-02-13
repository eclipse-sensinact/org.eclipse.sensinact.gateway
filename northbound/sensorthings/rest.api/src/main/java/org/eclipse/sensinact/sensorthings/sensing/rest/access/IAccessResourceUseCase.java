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
package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

public interface IAccessResourceUseCase {

    /**
     * return the resource snapshot identified by the id that is composed by
     * providerId, serviceId, resourceName separate by ~
     *
     * @param session
     * @param id
     * @return
     */
    public ResourceSnapshot read(SensiNactSession session, String id);

    public ResourceSnapshot read(SensiNactSession session, String providerId, String serviceId, String resourceName);

}
