/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class WebSocketRequestWrapper extends AbstractRequestWrapper {

    private final String content;
    private final ObjectMapper mapper;
    
    private JsonObject contentJson;
    
    public WebSocketRequestWrapper(String content, ObjectMapper mapper) {
        this.content = content;
        this.mapper = mapper;
    }
    
    private JsonObject getJson() {
    	if(content == null || content.isEmpty()) {
    		return null;
    	}
    	synchronized (this) {
			if(contentJson == null) {
				try {
		    		contentJson = mapper.readValue(content, JsonObject.class);
		    	} catch(Exception e) {
		    		LOG.config(String.format("Unable to convert the request content into a JSON object:\n %s",content));
		    	}
			}
			return contentJson;
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.RequestWrapper#getRequestURI()
     */
    @Override
    public String getRequestURI() {
    	JsonObject request = getJson();
    	if(request == null) {
    		return null;
    	} else {
	    	String uri = request.getString("uri", "");
	    	String[] uriElements = uri.split("\\?");
	    	return uriElements[0];
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.RequestWrapper#getQueryMap()
     */
    @Override
    public Map<String, List<String>> getQueryMap() {
    	JsonObject request = getJson();
    	if(request != null) {
            String uri = request.getString("uri", "");
            String[] uriElements = uri.split("\\?");       
	        if (uriElements.length == 2) {
	            try {
	                return AbstractRequestWrapper.processRequestQuery(uriElements[1]);
	            } catch (UnsupportedEncodingException e) {
	                LOG.log(Level.SEVERE, e.getMessage(),e);
	            }
	        }
    	}
        return Collections.<String, List<String>>emptyMap();
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.RequestWrapper#getContent()
     */
    @Override
    public String getContent() {
        return this.content;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.RequestWrapper#getAttributes()
	 */
	@Override
	public Map<String, List<String>> getAttributes() {
	    Map<String, List<String>> map = new HashMap<String, List<String>>();
	    JsonObject request = getJson();
    	if(request != null) {
            request.remove("uri");
            for(Entry<String, JsonValue> en : request.entrySet()) {
            	map.put(en.getKey(), Collections.singletonList(String.valueOf(en.getValue())));
            }
		}
		return map;
	}
}
