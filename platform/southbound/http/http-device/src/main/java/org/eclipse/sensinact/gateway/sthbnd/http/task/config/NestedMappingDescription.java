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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * MappingContext describes how to map path described data 
 * structures to sensiNact's inner data model
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NestedMappingDescription extends MappingDescription{

	@JsonProperty(value="path")
	private String path;

	@JsonProperty(value="mapping")
	private Map<String,String> mapping;
	
	public NestedMappingDescription() {}
	
	public NestedMappingDescription(String path, Map<String,String> mapping) {
		this.path = path;
		this.mapping = mapping;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the mapping
	 */
	public Map<String,String> getMapping() {
		return this.mapping;
	}

	/**
	 * @param mapping
	 */
	public void setMapping(Map<String,String> mapping) {
		this.mapping = mapping;
	}
	
	
	@Override
	public int getMappingType() {
		return MappingDescription.NESTED;
	}
}
