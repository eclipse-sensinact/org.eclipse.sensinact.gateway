/*********************************************************************
* Copyright (c) 2021 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.http.callback;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class HttpResponseWrapper extends HttpServletResponseWrapper implements ResponseWrapper {

	private AbstractResponseWrapper responseWrapper;

	/**
     * Constructor
     *
     * @param response  the {@link HttpServletResponse} to be wrapped by
     * the HttpResponseWrapper to be instantiated
     */
    public HttpResponseWrapper(HttpServletResponse response) {
        super(response);
        this.responseWrapper = new AbstractResponseWrapper() {
			@Override
			public void flush() {
				if(HttpResponseWrapper.this.isCommitted()) {
					return;
				}
				if(this.attributes != null) {
					for(String name :this.attributes.keySet()) {						
						try {
							HttpResponseWrapper.this.addHeader(name, AbstractResponseWrapper.getParameter(attributes, name));
						} catch (UnsupportedEncodingException e) {
							AbstractResponseWrapper.LOG.log(Level.WARNING,e.getMessage());
						}
					}
				}
				int length = this.content == null ? 0 : this.content.length;
			    if (length > 0) {
			    	HttpResponseWrapper.this.setContentLength(length);
			    	HttpResponseWrapper.this.setBufferSize(length);
			        ServletOutputStream output;
					try {
						output = HttpResponseWrapper.this.getOutputStream();
						output.write(this.content);
					} catch (IOException e) {
						AbstractResponseWrapper.LOG.log(Level.SEVERE, e.getMessage(),e);
					}
			    }
			    if(this.statusCode > 0) {
			    	HttpResponseWrapper.this.setStatus(this.statusCode);
			    } else {
			    	HttpResponseWrapper.this.setStatus(200);
			    }
			}
        };
    }
	
	@Override
	public void setAttributes(Map<String, List<String>> attributes) {
		this.responseWrapper.setAttributes(attributes);
	}

	@Override
	public void setContent(String content) {
		this.responseWrapper.setContent(content);
	}

	@Override
	public void setContent(byte[] bytes) {
		this.responseWrapper.setContent(bytes);
	}

	@Override
	public void setError(Throwable t) {	
		this.responseWrapper.setError(t);
	}
	
	@Override
	public void setError(int status, String message) {
		this.responseWrapper.setError(status,message);
	}

	@Override
	public void setResponseStatus(int status) {
		this.responseWrapper.setResponseStatus(status);
	}
	
	@Override
	public void flush() {
		this.responseWrapper.flush();
	}
}
