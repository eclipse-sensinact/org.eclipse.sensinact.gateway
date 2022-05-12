/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.SelectorIface;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonArray;

/**
 * This processor will receive a JSON Array (e.g. ["alpha","bravo"]) and enables to select one of the element based on its index.
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorFormatArray implements ProcessorFormatIface {
	
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
    @Override
    public String getName() {
        return "array";
    }

    @Override
    public String process(String inData, SelectorIface selector) throws ProcessorFormatException {
        JsonArray array;
		try {
			array = mapper.readValue(inData, JsonArray.class);
		} catch (JsonProcessingException e) {
			throw new ProcessorFormatException("JSON parsing failed", e);
		}
        if (selector.getExpression().equals("last")) {
            return array.get(array.size() - 1).toString();
        } else {
            Integer index = Integer.valueOf(selector.getExpression());
            return array.get(index).toString();
        }
    }
}
