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

import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provides access to Provider snapshots via a {@link ContextResolver}
 */
@Provider
public class AccessProviderUseCaseProvider implements ContextResolver<IAccessProviderUseCase> {

    @Override
    public IAccessProviderUseCase getContext(Class<?> type) {
        return (session, id) -> UtilDto.getProviderSnapshot(session, id).orElse(null);
    }

}
