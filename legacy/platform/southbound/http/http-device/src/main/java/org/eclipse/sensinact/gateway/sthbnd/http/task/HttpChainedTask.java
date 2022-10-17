/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;

/**
 *
 */
public abstract class HttpChainedTask<REQUEST extends Request<SimpleHttpResponse>> extends HttpTaskImpl<SimpleHttpResponse, REQUEST> implements Executable<Object, Void> {
    
    /**
     * @param command
     * @param transmitter
     * @param requestType
     * @param path
     * @param profileId
     * @param resourceConfig
     * @param parameters
     */
    public HttpChainedTask(CommandType command, TaskTranslator transmitter, Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(command, transmitter, requestType, path, profileId, resourceConfig, parameters);
    }

    
    @Override
    public boolean isDirect() {
        return true;
    }
}
