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
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractAccessMethodTrigger implements AccessMethodTrigger {
	
	public abstract String doGetJSON();
	
	private final String argumentBuilder;
	private final Object argument;
	private boolean passOn;

	protected AbstractAccessMethodTrigger(Object argument, String argumentBuilder, boolean passOn){
		this.argumentBuilder = argumentBuilder;
		this.argument = argument;
		this.passOn = passOn;
	}
	
	/**
	 * @return
	 */
	public <T> T getArgument() {
		return (T) this.argument;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTrigger#getArgumentBuilder()
	 */
	@Override
	public String getArgumentBuilder() {
		return this.argumentBuilder;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTrigger#passOn()
	 */
	@Override
	public boolean passOn() {
		return this.passOn;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JSONUtils.OPEN_BRACE);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_TYPE_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(this.getName());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_PASSON_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(this.passOn);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_BUILDER_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(this.getArgumentBuilder());
		buffer.append(JSONUtils.QUOTE);		
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_ARGUMENT_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.toJSONFormat(this.argument));
		
		
		String  json = this.doGetJSON();
		if(json != null && json.trim().length()>0) {
			buffer.append(JSONUtils.COMMA);
			buffer.append(json);
		}
		buffer.append(JSONUtils.CLOSE_BRACE);
		return buffer.toString();
	}
	
}
