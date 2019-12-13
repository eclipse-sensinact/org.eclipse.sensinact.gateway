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
package org.eclipse.sensinact.gateway.api.message;

import org.eclipse.sensinact.gateway.core.message.MessageFilter;

/**
 * Propagate {@link SnaMessage}s to a set of registered message callbacks according 
 * to theiraccess rights and the potential filter applying
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface MessagePropagator {
	/**
	 * Propagates the {@link SnaMessage} passed as parameter to all registered
	 * {@link MessageCallback}s according to the {@link MessageFilter} to which they 
	 * are mapped and their access rights
	 * 
	 * @param message the {@link SnaMessage} to be propagated
	 */
	void propagate(SnaMessage<?> message);

	/**
	 * Deletes the registered {@link MessageCallback} whose String identifier is 
	 * passed as parameter
	 * 
	 * @param callback string identifier of the {@link MessageCallback} to delete
	 */
	void deleteCallback(String callback);

	/**
	 * Returns the number of registered {@link MessageFilter}s whose filter field is
	 * equals to the one passed as parameter
	 * 
	 * @param filter the filter for which to calculate the number of registered
	 * {@link MessageFilter}(s)
	 * @return the number of registered {@link MessageFilter}s whose filter field is
	 * equals to the specified one
	 */
	public int count(String filter);

	/**
	 * Adds the {@link MessageCallback} passed as parameter and maps it to the specified
	 * {@link MessageFilter} allowing to discriminate {@link SnaMessage}s to transmit
	 * 
	 * @param filter the {@link MessageFilter} to map to the {@link MessageCallback} to add
	 * @param callback the {@link MessageCallback} to add
	 */
	void addCallback(MessageFilter filter, MessageCallback callback);

	/**
	 * Closes this MessagePropagator.
	 *
	 * @param wait defines whether to wait for the entire stack of messages
	 * processing before the effective close operation
	 */
	void close(boolean wait);
}
