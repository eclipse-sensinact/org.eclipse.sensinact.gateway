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
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

public class WebSocketRequestWrapper extends AbstractRequestWrapper {

    private String content;
    

    public WebSocketRequestWrapper(String content) {
        this.content = content;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.RequestWrapper#getRequestURI()
     */
    @Override
    public String getRequestURI() {
    	try {
    		JSONObject request = new JSONObject(content);
            String uri = request.optString("uri");
            String[] uriElements = uri.split("\\?");
            return uriElements[0];            
    	}catch(JSONException e) {
    		LOG.config(String.format("Unable to convert the request content into a JSON object:\n %s",content));
    	}
    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.RequestWrapper#getQueryMap()
     */
    @Override
    public Map<String, List<String>> getQueryMap() {
    	try {
    		JSONObject request = new JSONObject(content);
            String uri = request.optString("uri");
            String[] uriElements = uri.split("\\?");       
	        if (uriElements.length == 2) {
	            try {
	                return AbstractRequestWrapper.processRequestQuery(uriElements[1]);
	            } catch (UnsupportedEncodingException e) {
	                LOG.log(Level.SEVERE, e.getMessage(),e);
	            }
	        }
    	} catch(JSONException e) {
    		LOG.config(String.format("Unable to convert the request content into a JSON object:\n %s",content));
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
		if (this.content != null && this.content.length() > 0) {
			try {
	    		JSONObject request = new JSONObject(content);
	            request.remove("uri");
	            for(Iterator en = request.keys();en.hasNext();) {
	            	String key = (String) en.next();
	            	String value = String.valueOf(request.get(key));
	            	map.put(key, new ArrayList() {{ this.add (value);}});
	            }
	    	} catch(JSONException e) {
	    		LOG.config(String.format("Unable to convert the request content into a JSON object:\n %s",content));
	    	}
		}
		return map;
	}
}
