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

import org.eclipse.sensinact.core.push.DataUpdate;

import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provide access to the DataUpdate service via a {@link ContextResolver}
 */
@Provider
public class DataUpdateProvider implements ContextResolver<DataUpdate> {

    private final DataUpdate dataUpdate;

    public DataUpdateProvider(DataUpdate dataUpdate) {
        this.dataUpdate = dataUpdate;
    }

    @Override
    public DataUpdate getContext(Class<?> type) {
        return dataUpdate;
    }

}
