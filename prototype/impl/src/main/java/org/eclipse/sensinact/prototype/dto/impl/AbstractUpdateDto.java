/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.dto.impl;

import java.time.Instant;

public abstract class AbstractUpdateDto {

	public String model;
	
	public String provider;
	
	public String service;
	
	public String resource;
	
	public Instant timestamp;
}
