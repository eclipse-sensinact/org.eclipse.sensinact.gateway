/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.json;

import java.util.ServiceLoader;

import org.eclipse.parsson.api.BufferPool;

import jakarta.json.spi.JsonProvider;

/**
 * This class is a hack to work around the issues with jakarta.json in OSGi, 
 * pending a fix with a specification
 */
public class JsonProviderFactory {

	public static JsonProvider getProvider() {
		return ServiceLoader.load(JsonProvider.class, BufferPool.class.getClassLoader()).iterator().next();
		
	}
	
}
