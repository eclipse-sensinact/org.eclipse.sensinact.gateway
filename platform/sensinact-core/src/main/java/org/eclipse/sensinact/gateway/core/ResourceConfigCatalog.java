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

import java.util.List;

/**
 * Stores and provides the set of available {@link ResourceConfig}s for a
 * specific southbound bridge
 * 
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface ResourceConfigCatalog {
	/**
	 * Returns the {@link ResourceConfig} described by the
	 * {@link ResourceDescriptor} passed as parameter
	 * 
	 * @param resourceConfigDescriptor
	 *            the {@link ResourceDescriptor} describing the
	 *            {@link ResourceConfig} to be returned
	 * 
	 * @return the {@link ResourceConfig} described by the specified
	 *         {@link ResourceDescriptor}
	 */
	<G extends ResourceConfig, D extends ResourceDescriptor> G getResourceConfig(D resourceConfigDescriptor);

	/**
	 * Returns the List of {@link ResourceConfig}s for the service whose name is
	 * passed as parameter
	 * 
	 * @return the list of {@link ResourceConfig}s for the specified service
	 */
	<G extends ResourceConfig> List<G> getResourceConfigs(String profile, String serviceName);

	/**
	 * Returns the ResourceConfig of the Resource "by default" of the service whose
	 * name is passed as parameter
	 * 
	 * @param serviceName
	 *            the name of the service for which to retrieve the default
	 *            {@link ResourceConfig}
	 * 
	 * @return the default {@link ResourceConfig} for the specified service
	 */
	<G extends ResourceConfig> G getDefaultResourceConfig(String profile, String serviceName);
}
