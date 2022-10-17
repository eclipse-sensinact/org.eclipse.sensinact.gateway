/*********************************************************************
* Copyright (c) 2020 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * An abstract {@link ResponseWrapper} implementation
 *  
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public abstract class AbstractResponseWrapper implements ResponseWrapper {
	
	protected static final Logger LOG = Logger.getLogger(ResponseWrapper.class.getName());
	
	/**
     * Convert into a String the values List mapped to the String name
     * passed as parameter from the Map also passed as parameter
     *
     * @param map the map holding the parameter values List to be converted 
     * into a String
     * @param name the String name of the parameter 
     * 
     * @throws UnsupportedEncodingException
     */
    protected static String getParameter(Map<String, List<String>> map, String name) throws UnsupportedEncodingException {
        String parameter = null;
        if (name == null || name.length() == 0) {
        	return parameter;
        } else {
            name = URLDecoder.decode(name, "UTF-8");
        }
        List<String> values = map.get(name);
        if(values == null) {
        	return parameter;
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for(String value:values) {
        	if(!first) {
        		builder.append(' ');
        	}
    		builder.append(value);
        	first=false;
        }
        parameter = builder.toString();
        return parameter;
    }

	protected int statusCode;
	protected byte[] content;
    protected Map<String, List<String>> attributes;

    /* (non-Javadoc)
     * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#setAttributes(java.util.Map)
     */
    @Override
	public void setAttributes(Map<String, List<String>> attributes) {
		this.attributes = attributes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#setContent(java.lang.String)
	 */
	@Override
	public void setContent(String content) {
		if(content == null) {
			this.content = new byte[0];
			return;
		}
		this.content = content.getBytes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#setContent(byte[])
	 */
	@Override
	public void setContent(byte[] content) {
		if(content == null || content.length==0) {
			this.content = new byte[0];
			return;
		}
		this.content = new byte[content.length];
		System.arraycopy(content, 0, this.content, 0, content.length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#setError(java.lang.Throwable)
	 */
	@Override
	public void setError(Throwable t) {	
		setError(500,t==null?null:(t.getMessage()==null
			?t.getClass().getName():t.getMessage()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#setError(int, java.lang.String)
	 */
	@Override
	public void setError(int status, String message){
		this.statusCode = status;
		this.content = (message==null?"An error occurred":message).getBytes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#setResponseStatus(int)
	 */
	@Override
	public void setResponseStatus(int status) {
		this.statusCode = status;
	}
}
