/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.pull;

import org.eclipse.sensinact.prototype.annotation.propertytype.ProviderName;
import org.eclipse.sensinact.prototype.annotation.propertytype.WhiteboardResource;
import org.eclipse.sensinact.prototype.annotation.verb.GET;
import org.osgi.service.component.annotations.Component;

/**
 * Service properties define the provider that this pull based resource is for
 */
@WhiteboardResource
@ProviderName("pull_based")
@Component(service = _01_SimplePullBasedResource.class)
public class _01_SimplePullBasedResource {

	/**
	 * A GET method for a service and resource
	 * @return
	 */
	@GET(service = "example", resource = "default")
	public Double getValue() {
		// Get the value from the sensor
		return null;
	}
}
