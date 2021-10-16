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
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

/**
 * 
 * MappingDescription defines how path described data 
 * structures are mapped to sensiNact's inner data model
 */
public abstract class MappingDescription {

	public static final int ROOT = 1;
	public static final int NESTED = 2;
	
	/**
	 * @return
	 */
	public abstract int getMappingType();
	
}
