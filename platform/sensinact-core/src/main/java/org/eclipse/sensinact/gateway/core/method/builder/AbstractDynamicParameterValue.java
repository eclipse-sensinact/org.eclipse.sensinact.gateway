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
package org.eclipse.sensinact.gateway.core.method.builder;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;

/**
 * Abstract implementation of a {@link DynamicParameterValue}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractDynamicParameterValue implements DynamicParameterValue {
	protected final String resourceName;
	protected final String parameterName;

	protected final Mediator mediator;
	private Executable<Void, Object> resourceValueExtractor;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param parameterName
	 * @param resourceName
	 */
	protected AbstractDynamicParameterValue(Mediator mediator, String parameterName, String resourceName) {
		this.mediator = mediator;
		this.parameterName = parameterName;
		this.resourceName = resourceName;
	}

	/**
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValue#getResource()
	 */
	@Override
	public String getResource() {
		return this.resourceName;
	}

	/**
	 * Returns the Object value of the linked {@link ResourceImpl}
	 * 
	 * @return the linked {@link ResourceImpl}'s Object value
	 */
	public Object getResourceValue() {
		if (this.resourceValueExtractor != null) {
			try {
				return this.resourceValueExtractor.execute(null);
			} catch (Exception e) {
				if (this.mediator.isErrorLoggable()) {
					this.mediator.error(e, e.getMessage());
				}
			}
		}
		return null;
	}

	/**
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValue# setResourceValueExtractor(Executable)
	 */
	public void setResourceValueExtractor(Executable<Void, Object> resourceValueExtractor) {
		this.resourceValueExtractor = resourceValueExtractor;
	}
}
