/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.exception.ProcessorFormatException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;
import org.json.JSONObject;

import java.util.StringTokenizer;

/**
 * ProcessorFormat plugin that accepts as entry an JSON value and received as expression to select an specific value inside the JSON. e.g. assume
 * the json A ({"response":{"services":["admin","batteryLevel","illuminance","temperature:ambient"],"name":"smartsantander_u7jcfa_t2530"},"statusCode":200,"type":"DESCRIBE_RESPONSE","uri":"/smartsantander_u7jcfa_t2530"})
 * using the inData value below you will retrieve:
 * inData 'statusCode' you ll get '200'
 * inData 'response.name' you ll get 'smartsantander_u7jcfa_t2530'
 * etc.
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ProcessorFormatJSON implements ProcessorFormatIface {
    @Override
    public String getName() {
        return "json";
    }

    public String jsonDepthSearch(String elementPath,String json){

        StringTokenizer st=new StringTokenizer(elementPath,".");
        JSONObject rootJSON=new JSONObject(json);
        JSONObject current=rootJSON;
        while(st.hasMoreElements()){
            String val=st.nextToken();
            if(st.hasMoreElements()){
                current=current.getJSONObject(val);
            }else {
                return new String(current.get(val).toString());
            }
        }
        return current.toString();
    };

    @Override
    public String process(String inData,SelectorIface selector) throws ProcessorFormatException {
        return jsonDepthSearch(selector.getExpression(),inData);
    }
}
