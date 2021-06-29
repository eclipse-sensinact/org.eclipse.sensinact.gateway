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

import org.osgi.dto.DTO;

/**
 * A simple object representing a temporal data point available in the database. 
 * Each temporal point is described by its timestamp, unique tagID and value. 
 * Any database error during the retrieval of the temporal point is hold in the error field. 
 */
public class TemporalDTO extends DTO {
	
	public String error;
	
	public long timestamp;

	public int tagID;
	
	public String value;

}
