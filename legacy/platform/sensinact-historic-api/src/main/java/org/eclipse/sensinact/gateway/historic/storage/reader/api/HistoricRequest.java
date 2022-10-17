/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.historic.storage.reader.api;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * HistoricRequest allows to collect a set of points from a connected 
 * historic database depending on a targeted path and a time window defined
 * using a {@link ZonedDateTime} start time and a {@link ZonedDateTime} end time 
 */
public interface HistoricRequest<T> {
			
	void addTargetResource(String provider, String service, String resource);
	
	Map<String, T> execute();

}
