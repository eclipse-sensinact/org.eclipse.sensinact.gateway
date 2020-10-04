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
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.parser.MethodDefinition;
import org.eclipse.sensinact.gateway.generic.parser.ParameterDefinition;
import org.eclipse.sensinact.gateway.generic.parser.SignatureDefinition;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

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

    /**
     * @inheritDoc
     * @see HttpTaskConfigurator#
     * configure(HttpTask, java.lang.Object[])
     */
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) throws Exception {
        ResourceConfig resourceConfig = task.getResourceConfig();

        JSONObject json = new JSONObject();
        json.put("jsonrpc", "2.0");
        json.put("method", resourceConfig.getName());
        json.put("id", task.getTaskIdentifier());

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
                        JSONObject params = new JSONObject();
                        for (int i = 0; i < parametersName.size(); i++) {
                            params.put(parametersName.get(i).getName(), parameters[i]);
                        }
                        json.put("params", params);
                        break;
                    }
                    break;
                }
            }

        } else {
            Object params = kodiApi.getContent(parameters);
            if (params != null) {
                json.put("params", params);
            }
        }
        task.setContent(json.toString());
    }
}
