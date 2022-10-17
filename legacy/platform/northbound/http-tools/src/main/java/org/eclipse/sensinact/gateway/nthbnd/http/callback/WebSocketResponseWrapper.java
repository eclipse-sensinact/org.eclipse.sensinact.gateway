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
import java.util.logging.Level;

import org.eclipse.sensinact.gateway.nthbnd.http.callback.internal.CallbackWebSocketServlet;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class WebSocketResponseWrapper extends AbstractResponseWrapper {
   
	final CallbackWebSocketServlet wrapper;
	private final ObjectMapper mapper;
	
	public WebSocketResponseWrapper(CallbackWebSocketServlet wrapper, ObjectMapper mapper){
		this.wrapper = wrapper;
		this.mapper = mapper;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#flush()
	 */
	@Override
	public void flush() {
		JsonObjectBuilder attrs=null; 
		if(super.attributes != null && !super.attributes.isEmpty()) {
			attrs = JsonProviderFactory.getProvider()
					.createObjectBuilder();
			for(String key : super.attributes.keySet()) {
				try {
					attrs.add(key, AbstractResponseWrapper.getParameter(super.attributes, key));
				} catch (UnsupportedEncodingException e) {
					AbstractResponseWrapper.LOG.log(Level.SEVERE, e.getMessage(),e);
				}
			}
		}
		if(super.content != null && super.content.length > 0) {
			try {
				JsonObjectBuilder builder = JsonProviderFactory.getProvider()
						.createObjectBuilder(mapper.readValue(super.content, JsonObject.class));
				if(attrs != null) {
					builder.add("attributes", attrs);
				}
				if(super.statusCode > 0) {
					builder.add("statusCode", super.statusCode);
				}
				this.wrapper.writeMessage(mapper.writeValueAsString(builder.build()));
			}catch(Exception e) {
				this.wrapper.writeMessage(new String(content));
			}
			return;
		}
		if(attrs != null) {
			if(super.statusCode > 0) {
				attrs.add("statusCode", super.statusCode);
			}
			try {
				this.wrapper.writeMessage(mapper.writeValueAsString(attrs.build()));
			} catch (JsonProcessingException e) {
				AbstractResponseWrapper.LOG.log(Level.SEVERE, e.getMessage(),e);
				this.wrapper.writeMessage("500");
			}
			return;
		}
		if(super.statusCode > 0) {
			this.wrapper.writeMessage(String.valueOf(super.statusCode));
		}
	}
}
