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

import java.util.StringTokenizer;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.selector.SelectorIface;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

/**
 * ProcessorFormat plugin that accepts as entry an JSON value and received as expression to select an specific value inside the JSON. e.g. assume
 * the json A ({"response":{"services":["admin","batteryLevel","illuminance","temperature:ambient"],"name":"smartsantander_u7jcfa_t2530"},"statusCode":200,"type":"DESCRIBE_RESPONSE","uri":"/smartsantander_u7jcfa_t2530"})
 * using the inData value below you will retrieve:
 * inData 'statusCode' you ll get '200'
 * inData 'response.name' you ll get 'smartsantander_u7jcfa_t2530'
 * etc.
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorFormatJSON implements ProcessorFormatIface {
	
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
    @Override
    public String getName() {
        return "json";
    }

    public String jsonDepthSearch(String elementPath, String json) throws JsonProcessingException {
        StringTokenizer st = new StringTokenizer(elementPath, ".");
        JsonObject rootJSON = mapper.readValue(json, JsonObject.class);
        JsonObject current = rootJSON;
        while (st.hasMoreElements()) {
            String val = st.nextToken();
            if (st.hasMoreElements()) {
                current = current.getJsonObject(val);
            } else {
                return new String(current.get(val).toString());
            }
        }
        return current.toString();
    }

    ;

    @Override
    public String process(String inData, SelectorIface selector) throws ProcessorFormatException {
        try {
			return jsonDepthSearch(selector.getExpression(), inData);
		} catch (JsonProcessingException e) {
			throw new ProcessorFormatException("JSON parsing failure", e);
		}
    }
}
