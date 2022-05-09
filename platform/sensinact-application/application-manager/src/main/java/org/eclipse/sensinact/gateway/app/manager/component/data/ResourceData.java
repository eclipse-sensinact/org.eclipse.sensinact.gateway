/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component.data;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;

/**
 * This class acts as a proxy to a sNa resource
 *
 * @author Rémi Druilhe
 */
public class ResourceData implements DataItf {
    private final Session session;
    private final String uri;
    
    private GetResponse last;

    public ResourceData(Session session, String uri) {
        this.session = session;
        this.uri = uri;
    }

    /**
     * Get the source sensiNact URI of this resource
     *
     * @return the URI of the resource
     */
    public String getSourceUri() {
        return uri;
    }

    private DescribeResponse<JSONObject> describe() {
    	String[] uriElements = UriUtils.getUriElements(getSourceUri());
        if (uriElements.length != 3) {
            return null;
        }
        DescribeResponse<JSONObject> response = this.session.getResource(uriElements[0], uriElements[1], uriElements[2]);
        if(response == null || response.getStatusCode()!=200) {
        	 return null;
        }
        return response;
    } 
    
    public GetResponse get() {
    	String[] uriElements = UriUtils.getUriElements(getSourceUri());
        if (uriElements.length != 3) {
            return null;
        }
        GetResponse response = this.session.get(uriElements[0], uriElements[1], uriElements[2],DataResource.VALUE);
        if(response == null || response.getStatusCode()!=200) {
        	this.last = null;
        	 return null;
        }
        this.last = response;
        return response;
    } 

    public SetResponse set(Object value) {
    	String[] uriElements = UriUtils.getUriElements(getSourceUri());
        if (uriElements.length != 3) {
            return null;
        }
        SetResponse response = this.session.set(uriElements[0], uriElements[1], uriElements[2],DataResource.VALUE, value);
        return response;
    } 

    public ActResponse act(Object[] parameters) {
    	String[] uriElements = UriUtils.getUriElements(getSourceUri());
        if (uriElements.length != 3) {
            return null;
        }
        ActResponse response = this.session.act(uriElements[0], uriElements[1], uriElements[2],parameters);
        return response;
    } 
    
    /**
     * Get the value of the {@link Resource}
     *
     * @return the value
     */
    public Object getValue() {
    	GetResponse response = get();
    	if(response == null) {
    		this.last = null;
    		return null;
    	}
    	this.last = response;
        return response.getResponse(DataResource.VALUE);
    }

    /**
     * Get the Java type of the {@link Resource}
     *
     * @return the Java type
     */
    public Class<?> getType() {
    	if(this.last == null) {
	    	get();
    	}
    	if(last == null) {
    		return Object.class;
    	}
        return CastUtils.jsonTypeToJavaType((String) last.getResponse(DataResource.TYPE));
    }

    /**
     * Get the ResourceType of the {@link Resource}
     *
     * @return the String ResourceType
     */
    public String getResourceType() {
    	DescribeResponse<JSONObject> response = describe();
    	if(response == null) {
    		return null;
    	}
        return (String) response.getResponse().get(DataResource.TYPE);
    }

    /**
     * Get the timestamp of the data
     *
     * @return the timestamp of the data
     */
    public long getTimestamp() {
    	if(this.last == null) {
	    	get();
    	}
    	if(last == null) {
    		return 0l;
    	}
        return last.getResponse(Long.class, Metadata.TIMESTAMP);
    }
}
