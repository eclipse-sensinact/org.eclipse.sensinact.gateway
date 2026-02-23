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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.sensorthings.sensing.rest.IExtraDelegate;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;

/**
 * Provides the IExtraDelegate service to a Jakarta REST application
 */
@Provider
public class ExtraDelegateProvider implements ContextResolver<IExtraDelegate> {

    @Context
    Providers providers;

    private ExtraDelegateImpl extraDelegateImpl;

    @Override
    public IExtraDelegate getContext(Class<?> type) {
        IExtraDelegate result;
        synchronized (this) {
            if (extraDelegateImpl == null) {
                extraDelegateImpl = new ExtraDelegateImpl(providers);
            }
            result = extraDelegateImpl;
        }
        return result;
    }
}
