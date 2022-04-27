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
import org.json.JSONException;
import org.json.JSONObject;

public class WebSocketResponseWrapper extends AbstractResponseWrapper {
   
	CallbackWebSocketServlet wrapper;
	
	public WebSocketResponseWrapper(CallbackWebSocketServlet wrapper){
		this.wrapper = wrapper;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.sensinact.gateway.nthbnd.http.tools.internal.ResponseWrapper#flush()
	 */
	@Override
	public void flush() {
		JSONObject attrs=null; 
		if(super.attributes != null && !super.attributes.isEmpty()) {
			attrs = new JSONObject();
			for(String key :super.attributes.keySet()) {
				try {
					attrs.put(key, AbstractResponseWrapper.getParameter(super.attributes, key));
				} catch (JSONException | UnsupportedEncodingException e) {
					AbstractResponseWrapper.LOG.log(Level.SEVERE, e.getMessage(),e);
				}
			}
		}
		if(super.content != null && super.content.length > 0) {
			try {
				JSONObject obj = new JSONObject(new String(super.content));
				if(attrs != null) {
					obj.put("attributes", attrs);
				}
				if(super.statusCode > 0) {
					obj.put("statusCode", super.statusCode);
				}
				this.wrapper.writeMessage(obj.toString());
			}catch(JSONException e) {
				this.wrapper.writeMessage(new String(content));
			}
			return;
		}
		if(attrs != null) {
			if(super.statusCode > 0) {
				attrs.put("statusCode", super.statusCode);
			}
			this.wrapper.writeMessage(attrs.toString());
			return;
		}
		if(super.statusCode > 0) {
			this.wrapper.writeMessage(String.valueOf(super.statusCode));
		}
	}
}
