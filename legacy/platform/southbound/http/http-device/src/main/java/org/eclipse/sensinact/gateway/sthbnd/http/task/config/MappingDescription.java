/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import java.util.Map;

/**
 * 
 * MappingDescription defines how path described data 
 * structures are mapped to sensiNact's inner data model
 */
public abstract class MappingDescription {

	public static final int ROOT = 1;
	public static final int NESTED = 2;
	
	/**
	 * Return the type of the described mapping : 
	 * <ul>
	 * 		<li>ROOT or</li>
	 * 		<li>NESTED</li>
	 * </ul>
	 * 
	 * @return the type of mapping described by this MappingDescription
	 */
	public abstract int getMappingType();
	
	
	public abstract Map<String, String> getMapping();
}
