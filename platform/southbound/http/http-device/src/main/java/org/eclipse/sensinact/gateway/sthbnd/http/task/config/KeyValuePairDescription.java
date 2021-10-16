/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.task.config;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.sthbnd.http.annotation.KeyValuePair;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class KeyValuePairDescription {

    public static List<KeyValuePairDescription> toDescription(KeyValuePair[] keyValuePairs) {
    	List<KeyValuePairDescription> description = new ArrayList<>();
    	if(keyValuePairs == null ||keyValuePairs.length == 0)
    		return description;
    	for(KeyValuePair pair : keyValuePairs)
    		description.add(toDescription(pair));
    	return description;
    }
    
    public static KeyValuePairDescription toDescription(KeyValuePair keyValuePair) {
    	KeyValuePairDescription description = new KeyValuePairDescription();
    	description.setKey(keyValuePair.key());
    	description.setValue(keyValuePair.value());
    	description.setOperator(keyValuePair.operator());
    	return description;
    }
    
	private static String DEFAULT_OPERATOR = "=";
	
	@JsonProperty(value="key")
    private String key;

	@JsonProperty(value="value")
    private String value;

	@JsonProperty(value="operator")
    private String operator;
	
	public KeyValuePairDescription() {}
	
	public KeyValuePairDescription(String key, String value, String operator) {
		this.key = key;
		this.value = value;
		this.operator = operator;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		if(operator==null) 
			return DEFAULT_OPERATOR;
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	
}
