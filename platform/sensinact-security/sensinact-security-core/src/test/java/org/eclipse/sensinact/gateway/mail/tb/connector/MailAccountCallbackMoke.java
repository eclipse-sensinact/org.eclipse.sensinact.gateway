/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.mail.tb.connector;

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
public class MailAccountCallbackMoke implements CallbackService{

	private String pattern;
	private Executable<CallbackContext, Void> processor;
	private Dictionary<String, Object> props;

	/**
	 * 
	 */
	public MailAccountCallbackMoke(String pattern, Dictionary<String, ?> properties, Executable<CallbackContext, Void> processor) {
		this.pattern = pattern;
		this.props = new Hashtable<>();
		if(properties != null) {
			Enumeration<String> enumeration = properties.keys();
			while(enumeration.hasMoreElements()) {
				String key = enumeration.nextElement();
				this.props.put(key, properties.get(key));
			}
		}
		this.processor = processor;
	}

	@Override
	public String getPattern() {
		return this.pattern;
	}

	@Override
	public Dictionary<String, ?> getProperties() {
		return this.props;
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
