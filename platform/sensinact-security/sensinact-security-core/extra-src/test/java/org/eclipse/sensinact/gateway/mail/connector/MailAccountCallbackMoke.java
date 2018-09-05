/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
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
public class MailAccountCallbackMoke implements CallbackService{

	private String pattern;
	private Executable<CallbackContext, Void> processor;
	private Dictionary properties;

	/**
	 * 
	 */
	public MailAccountCallbackMoke(String pattern, Dictionary properties, Executable<CallbackContext, Void> processor) {
		this.pattern = pattern;
		if(properties != null) {
			this.properties = new Hashtable();
			Enumeration enumeration = properties.keys();
			while(enumeration.hasMoreElements()) {
				Object key = enumeration.nextElement();
				this.properties.put(key, properties.get(key));
			}
		} 
		this.processor = processor;
	}

	@Override
	public String getPattern() {
		return this.pattern;
	}

	@Override
	public Dictionary getProperties() {
		return this.properties;
	}

	@Override
	public Executable<CallbackContext, Void> getCallbackProcessor() {
		return this.processor;
	}

}
