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

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class JSONHttpChainedTasks<REQUEST extends Request<SimpleHttpResponse>> extends HttpChainedTasks<REQUEST, JSONHttpChainedTask<REQUEST>> {
    /**
     * @param mediator
     * @param transmitter
     * @param path
     * @param resourceConfig
     * @param parameters
     */
    public JSONHttpChainedTasks(CommandType command, TaskTranslator transmitter, Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(command, transmitter, requestType, path, profileId, resourceConfig, parameters);
    }

    /**
     * @InheritedDoc
     * @see HttpTask#getHttpMethod()
     */
    @Override
    public String getHttpMethod() {
        return HttpConnectionConfiguration.GET;
    }

    /**
     * @param uri
     * @param uri
     */
    public void addUri(String identifier, String uri) {
        JSONHttpChainedTask<REQUEST> task = this.createChainedTask(identifier, this.getCommand(), super.getPath(), super.getProfile(), super.getResourceConfig(), super.getParameters());

        task.setUri(uri);
        super.addChainedTask(task);
    }

    /**
     * @InheritedDoc
     * @see HttpChainedTasks#
     * createChainedTask(Task.CommandType,
     * java.lang.String, ResourceConfig,
     * java.lang.Object[], org.eclipse.sensinact.gateway.core.Executor)
     */
    @Override
    public JSONHttpChainedTask<REQUEST> createChainedTask(String identifier, CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        JSONHttpChainedTask<REQUEST> chain = new JSONHttpChainedTask<REQUEST>(command, transmitter, requestType, path, profileId, resourceConfig, parameters);

        chain.setChainedIdentifier(identifier);
        return chain;
    }

    /**
     * @inheritDoc 2
     * @see HttpChainedTasks#getResultBytes()
     */
    @Override
    protected byte[] getResultBytes() {
        Object result = null;

        if ((result = super.getIntermediateResult()) != null && result != AccessMethod.EMPTY) {
            if (JSONObject.class.isAssignableFrom(result.getClass())) {
                return ((JSONObject) result).toString().getBytes();
            }
            if (JSONArray.class.isAssignableFrom(result.getClass())) {
                return ((JSONArray) result).toString().getBytes();
            }
            if (String.class == result.getClass()) {
                return ((String) result).getBytes();
            }
        }
        return new byte[0];
    }

    /**
     * @inheritDoc
     * @see HttpTask#getOptions()
     */
    @Override
    public Map<String, List<String>> getOptions() {
        return null;
    }
}
