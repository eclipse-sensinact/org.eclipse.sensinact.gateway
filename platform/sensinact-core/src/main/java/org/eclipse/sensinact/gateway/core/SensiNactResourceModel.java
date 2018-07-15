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
package org.eclipse.sensinact.gateway.core;

import java.util.Dictionary;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.ProcessableData;

/**
 * The sensiNact resource model definition
 * 
 * @param <C>
 *            the extended {@link SensiNactResourceModelConfiguration} type in
 *            use
 */
public interface SensiNactResourceModel<C extends SensiNactResourceModelConfiguration> extends Nameable {
	/**
	 * Returns the {@link SensiNactResourceModelConfiguration} object configuring
	 * this SensiNactResourceModel instance
	 * 
	 * @return this SensiNactResourceModel instance's the
	 *         {@link SensiNactResourceModelConfiguration}
	 */
	C configuration();

	/**
	 * Returns true if this SensiNactResourceModel has been registered; returns
	 * false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if this SensiNactResourceModel has already been
	 *         registered</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	boolean isRegistered();

	/**
	 * Returns the {@link Mediator} of this SensiNactResourceModel allowing to
	 * interact with the OSGi host environment
	 * 
	 * @return this SensiNactResourceModel's {@link Mediator}
	 */
	Mediator mediator();

	/**
	 * Returns the root {@link ModelElement} of this SensinactResourceModel instance
	 * 
	 * @return this SensinactResourceModel instance's root {@link ModelElement}
	 */
	<I extends ModelInstance<?>, M extends ModelElementProxy, P extends ProcessableData, E extends Nameable, R extends Nameable> ModelElement<I, M, P, E, R> getRootElement();

	/**
	 * Returns the String identifier of this SensiNactResourceModel in the datastore
	 * of the sensiNact framework instance holding it
	 * 
	 * @return this SensinactResourceModel identifier in the framework datastore
	 */
	String getIdentifier();

	/**
	 * Returns the properties map to be registered in the OSGi host environment
	 * 
	 * @return the properties map to be registered in the OSGi host environment
	 */
	Dictionary<String, String> getProperties();

	/**
	 * 
	 */
	void unregister();

}
