/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.historic.storage.reader.api;

import java.time.ZonedDateTime;

/**
 * HistoricRequest allows to collect a set of points from a connected 
 * historic database depending on a targeted path and a time window defined
 * using a {@link ZonedDateTime} start time and a {@link ZonedDateTime} end time 
 */
public interface HistoricRequest<T> {
			
	void setServiceProviderIdentifier(String providerId);
	
	void setServiceIdentifier(String serviceId);

	void setResourceIdentifier(String resourceId);
	
	T execute();

}
