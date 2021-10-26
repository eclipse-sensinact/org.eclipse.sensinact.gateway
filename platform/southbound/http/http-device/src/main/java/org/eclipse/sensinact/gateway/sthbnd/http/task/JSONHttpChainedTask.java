/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.task;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class JSONHttpChainedTask<REQUEST extends Request<SimpleHttpResponse>> extends HttpChainedTask<REQUEST> {
	
	private static final Logger LOG = LoggerFactory.getLogger(JSONHttpChainedTask.class);
    /**
     *
     */
    protected JSONArray array;

    /**
     *
     */
    protected String uri;

    /**
     *
     */
    private String chainedIdentifier;

    /**
     * @param mediator
     * @param transmitter
     * @param path
     * @param resourceConfig
     * @param parameters
     */
    public JSONHttpChainedTask(Mediator mediator, CommandType command, TaskTranslator transmitter, Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(mediator, command, transmitter, requestType, path, profileId, resourceConfig, parameters);
    }

    /**
     * @param chainedIdentifier
     */
    public void setChainedIdentifier(String chainedIdentifier) {
        this.chainedIdentifier = chainedIdentifier;
    }

    /**
     * @return
     */
    public String getChainedIdentifier() {
        return this.chainedIdentifier;
    }

    /**
     * @param result
     * @throws JSONException
     */
    protected void append(Object result) {
        Object append = null;

        if (result == null) {
            return;
        }
        JSONObject object = new JSONObject();
        String chainedIdentifier = this.getChainedIdentifier();

        if (chainedIdentifier == null) {
            chainedIdentifier = Integer.toString(super.hashCode());
        }
        if (byte[].class.isAssignableFrom(result.getClass())) {
            append = new String((byte[]) result);

        } else if (result == AccessMethod.EMPTY) {
            append = new String(new byte[0]);

        } else {
            append = result;
        }
        Object jsonAppend = null;

        if (String.class == append.getClass()) {
            String appendStr = (String) append;
            try {
                jsonAppend = new JSONObject(appendStr);

            } catch (JSONException e) {
                try {
                    jsonAppend = new JSONArray(appendStr);

                } catch (JSONException ex) {
                    jsonAppend = appendStr;
                }
            }
        } else {
            jsonAppend = append;
        }
        try {
            object.put(chainedIdentifier, jsonAppend);

        } catch (JSONException e) {
            LOG.error("Unable to build the result object");
        }
        this.array.put(object);
    }

    /**
     * @inheritDoc
     * @see Executable#execute(java.lang.Object)
     */
    public Void execute(Object result) throws Exception {
        if (result == null || !JSONArray.class.isAssignableFrom(result.getClass())) {
            this.array = new JSONArray();
            this.append(result);

        } else {
            this.array = (JSONArray) result;
        }
        return null;
    }

    /**
     * @throws Exception
     * @inheritDoc
     * @see TaskImpl#setResult(java.lang.Object, long)
     */
    public void setResult(Object result, long timestamp) {
        this.append(result);
        super.setResult(this.array, timestamp);
    }

    /**
     * @inheritDoc
     * @see HttpTask#getHttpMethod()
     */
    @Override
    public String getHttpMethod() {
        return HttpConnectionConfiguration.GET;
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.uri;
    }


    /**
     * @inheritDoc
     * @see URITask#getOptions()
     */
    @Override
    public Map<String, List<String>> getOptions() {
        return super.getHeaders();
    }
}
