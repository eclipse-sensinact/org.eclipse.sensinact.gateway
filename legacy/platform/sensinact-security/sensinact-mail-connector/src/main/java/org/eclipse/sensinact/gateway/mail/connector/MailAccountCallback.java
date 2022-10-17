/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.mail.connector;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;

/**
 * @author christophe
 *
 */
public class MailAccountCallback implements CallbackService{

	private String pattern;
	private Executable<CallbackContext, Void> processor;
	private Dictionary<String,Object> properties;

	/**
	 * 
	 */
	public MailAccountCallback(String pattern, Dictionary<Object,Object> properties, Executable<CallbackContext, Void> processor) {
		this.pattern = pattern;
		if(properties != null) {
			this.properties = new Hashtable<String, Object>();
			Enumeration<Object> enumeration = properties.keys();
			while(enumeration.hasMoreElements()) {
				Object key = enumeration.nextElement();
				this.properties.put(key.toString(), properties.get(key));
			}
		} 
		this.processor = processor;
	}

	@Override
	public String getPattern() {
		return this.pattern;
	}

	@Override
	public Dictionary<String,Object> getProperties() {
		return this.properties;
	}

	@Override
	public void process(CallbackContext context) {
		try {
			this.processor.execute(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCallbackType() {
		return CallbackService.CALLBACK_SERVLET;
	}

}
