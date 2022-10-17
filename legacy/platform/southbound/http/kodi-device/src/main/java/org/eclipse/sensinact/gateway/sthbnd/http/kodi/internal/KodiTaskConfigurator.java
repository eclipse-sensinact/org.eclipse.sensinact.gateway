/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.parser.MethodDefinition;
import org.eclipse.sensinact.gateway.generic.parser.ParameterDefinition;
import org.eclipse.sensinact.gateway.generic.parser.SignatureDefinition;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class KodiTaskConfigurator implements HttpTaskConfigurator {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
    /**
     * @inheritDoc
     * @see HttpTaskConfigurator#
     * configure(HttpTask, java.lang.Object[])
     */
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        ResourceConfig resourceConfig = task.getResourceConfig();

        Map<String, Object> map = new HashMap<>();
        map.put("jsonrpc", "2.0");
        map.put("method", resourceConfig.getName());
        map.put("id", task.getTaskIdentifier());
        
        KodiApi kodiApi = KodiApi.fromName(resourceConfig.getName());
        
        Object[] parameters = task.getParameters();
        
        if (kodiApi == null) {
        	if (CommandType.ACT.equals(task.getCommand())) {
        		Iterator<MethodDefinition> iterator = ((ExtResourceConfig) resourceConfig).iterator();
        		
        		MethodDefinition actMethodDefinition = null;
        		
        		while (iterator.hasNext()) {
        			actMethodDefinition = iterator.next();
        			if (!actMethodDefinition.getType().equals(AccessMethod.Type.valueOf(AccessMethod.ACT))) {
        				continue;
        			}
        			Iterator<SignatureDefinition> it = actMethodDefinition.iterator();
        			
        			while (it.hasNext()) {
        				List<ParameterDefinition> parametersName = it.next().getParameters();
        				
        				if (parametersName.size() != parameters.length) {
        					continue;
        				}
        				
        				Map<String, Object> params = new HashMap<>();
        				
        				for (int i = 0; i < parametersName.size(); i++) {
        					map.put(parametersName.get(i).getName(), parameters[i]);
        				}
        				map.put("params", params);
        				break;
        			}
        			break;
        		}
        	}
        	
        } else {
        	JsonObject params = kodiApi.getContent(parameters);
        	if (params != null) {
        		map.put("params", params);
        	}
        }
        task.setContent(mapper.writeValueAsString(map));
    }
}
