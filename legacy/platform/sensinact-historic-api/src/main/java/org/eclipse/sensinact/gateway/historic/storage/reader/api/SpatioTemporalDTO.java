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

import java.time.Instant;

import org.osgi.dto.DTO;

/**
 * A simple object representing a spatio-temporal data point available in the database. 
 * Each spatial point is described by its timestamp, unique tagID, coordinates and value. 
 * Any database error during the retrieval of the spatio-temporal point is hold in the 
 * error field. 
 */
public class SpatioTemporalDTO extends DTO{
	
    public String error;

	public Instant timestamp;

	public int tagID;
	
	public String latitude, longitude;

	public String value;
    
}
