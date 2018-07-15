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
package org.eclipse.sensinact.gateway.core.message;

/**
 * Handler of {@link SnaMessage}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface MessageHandler {
	/**
	 * Treats the {@link SnaMessage} passed as parameter
	 * 
	 * @param event
	 *            the {@link SnaMessage} to treat
	 */
	void handle(SnaMessage message);

	/**
	 * Deletes the registered {@link MidCallback} identifiable using the callback
	 * object passed as parameter
	 * 
	 * @param callback
	 *            string identifier of the {@link MidCallback} to delete
	 * @return the number of {@link MidCallback}s that are still registered for the
	 *         same filter than the deleted {@link MidCallback}
	 */
	void deleteCallback(String callback);

	/**
	 * Returns the number of registered {@link SnaFilter}s whose filter field is
	 * equals to the one passed as parameter
	 * 
	 * @param filter
	 *            the filter for which to calculate the number of registered
	 *            SnaFilter
	 * @return the number of registered {@link SnaFilter}s whose filter field is
	 *         equals to the specified one
	 */
	public int count(String filter);

	/**
	 * Adds the {@link MidCallback} passed as parameter and maps it to the specified
	 * {@link SnaFilter} allowing to discriminate {@link SnaMessage}s to transmit
	 * 
	 * @param filter
	 *            the {@link SnaFilter} to map to the {@link MidCallback} to add
	 * @param callback
	 *            the {@link MidCallback} to add
	 */
	void addCallback(SnaFilter filter, MidCallback callback);

	/**
	 * Stops this MessageHandler.
	 *
	 * @param wait
	 *            defines whether to wait for the entire stack of messages
	 *            processing before closing or not
	 * 
	 */
	void close(boolean wait);
}
