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

import java.io.StringReader;
import java.util.ServiceLoader;

import org.eclipse.parsson.api.BufferPool;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.spi.JsonProvider;

/**
 * This class is a hack to work around the issues with jakarta.json in OSGi, 
 * pending a fix with a specification
 */
public class JsonProviderFactory {

	public static JsonProvider getProvider() {
		return ServiceLoader.load(JsonProvider.class, BufferPool.class.getClassLoader()).iterator().next();
		
	}
	
	public static <T extends JsonValue> T read(JsonProvider provider, String json, Class<T> type) {
		return type.cast(provider.createReader(new StringReader(json)).readValue());
	}
	
	public static <T extends JsonValue> T read(String json, Class<T> type) {
		return read(getProvider(), json, type);
	}
	
	public static JsonObject readObject(String json) {
		return read(getProvider(), json, JsonObject.class);
	}

	public static JsonArray readArray(String json) {
		return read(getProvider(), json, JsonArray.class);
	}

	public static JsonObject readObject(JsonProvider provider, String json) {
		return read(provider, json, JsonObject.class);
	}
	
	public static JsonArray readArray(JsonProvider provider, String json) {
		return read(provider, json, JsonArray.class);
	}
	
}
