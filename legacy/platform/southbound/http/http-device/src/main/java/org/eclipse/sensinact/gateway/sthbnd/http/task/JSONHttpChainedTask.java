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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.TaskImpl;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.generic.uri.URITask;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpConnectionConfiguration;
import org.eclipse.sensinact.gateway.sthbnd.http.SimpleHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 *
 */
public class JSONHttpChainedTask<REQUEST extends Request<SimpleHttpResponse>> extends HttpChainedTask<REQUEST> {
	
	private static final Logger LOG = LoggerFactory.getLogger(JSONHttpChainedTask.class);
    /**
     *
     */
    protected List<JsonValue> array;

    /**
     *
     */
    protected String uri;

    /**
     *
     */
    private String chainedIdentifier;
	private final ObjectMapper mapper;

    /**
     * @param transmitter
     * @param path
     * @param resourceConfig
     * @param parameters
     * @param mapper 
     */
    public JSONHttpChainedTask(CommandType command, TaskTranslator transmitter, Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters, ObjectMapper mapper) {
        super(command, transmitter, requestType, path, profileId, resourceConfig, parameters);
		this.mapper = mapper;
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

        if (result == null) {
            return;
        }
        
        try {
        	ObjectNode objectNode = mapper.createObjectNode();
        	
        	String chainedIdentifier = this.getChainedIdentifier();
        	
        	if (chainedIdentifier == null) {
        		chainedIdentifier = Integer.toString(super.hashCode());
        	}
        	
        	Object append = null;
        	if (byte[].class.isAssignableFrom(result.getClass())) {
        		append = new String((byte[]) result);
        		
        	} else if (result == AccessMethod.EMPTY) {
        		append = "";
        		
        	} else {
        		append = result;
        	}
        	
        	Object jsonAppend = null;
        	
        	if (String.class == append.getClass()) {
        		String appendStr = (String) append;
        		try {
        			jsonAppend = mapper.readValue(appendStr, JsonValue.class);
        		} catch (Exception e) {
        			jsonAppend = appendStr;
        		}
        	} else {
        		jsonAppend = append;
        	}
        	
        	objectNode.set(chainedIdentifier, mapper.convertValue(jsonAppend, JsonNode.class));
        	
        	this.array.add(mapper.convertValue(objectNode, JsonObject.class));

        } catch (Exception e) {
            LOG.error("Unable to build the result object", e);
        }
    }

    /**
     * @inheritDoc
     * @see Executable#execute(java.lang.Object)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Void execute(Object result) throws Exception {
        if (result == null || !List.class.isAssignableFrom(result.getClass())) {
            this.array = new ArrayList<>();
            this.append(result);

        } else {
            this.array = new ArrayList<>((List)result);
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
        super.setResult(mapper.convertValue(this.array, JsonArray.class), timestamp);
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
