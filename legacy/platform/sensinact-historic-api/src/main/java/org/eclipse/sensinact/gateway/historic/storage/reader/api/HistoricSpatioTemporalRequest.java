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
import java.util.List;

/**
 *
 */
public interface HistoricSpatioTemporalRequest extends HistoricRequest<List<SpatioTemporalDTO>> {

	void setHistoricStartTime(ZonedDateTime fromTime);

	void setHistoricEndTime(ZonedDateTime toTime);
	
	void setFunction(String function);	

	void setTemporalWindow(long period);
	
	void setRegion(String region);
	
}
