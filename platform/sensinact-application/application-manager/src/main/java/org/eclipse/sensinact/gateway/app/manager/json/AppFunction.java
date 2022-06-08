/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

import java.util.ArrayList;
import java.util.List;

public class AppFunction implements JSONable {
    private final String name;
    private final List<AppParameter> buildParameters;
    private final List<AppParameter> runParameters;

    public AppFunction(String name, List<AppParameter> buildParameters, List<AppParameter> runParameters) {
        this.name = name;
        this.buildParameters = buildParameters;
        this.runParameters = runParameters;
    }

    public AppFunction(JsonObject json) {
        this.name = json.getString(AppJsonConstant.APP_FUNCTION_NAME);
        this.buildParameters = new ArrayList<AppParameter>();
        if (json.containsKey(AppJsonConstant.APP_FUNCTION_BUILD_PARAMETERS)) {
            JsonArray parameterArray = json.getJsonArray(AppJsonConstant.APP_FUNCTION_BUILD_PARAMETERS);
            for (int i = 0; i < parameterArray.size(); i++) {
                buildParameters.add(new AppParameter(parameterArray.getJsonObject(i)));
            }
        }
        this.runParameters = new ArrayList<AppParameter>();
        if (json.containsKey(AppJsonConstant.APP_FUNCTION_RUN_PARAMETERS)) {
            JsonArray parameterArray = json.getJsonArray(AppJsonConstant.APP_FUNCTION_RUN_PARAMETERS);
            for (int i = 0; i < parameterArray.size(); i++) {
                runParameters.add(new AppParameter(parameterArray.getJsonObject(i)));
            }
        }
    }

    public String getName() {
        return name;
    }

    public List<AppParameter> getBuildParameters() {
        return buildParameters;
    }

    public List<AppParameter> getRunParameters() {
        return runParameters;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
    	JsonProvider provider = JsonProviderFactory.getProvider();
        JsonObjectBuilder function = provider.createObjectBuilder();
        function.add(AppJsonConstant.APP_FUNCTION_NAME, name);
        if (!buildParameters.isEmpty()) {
            JsonArrayBuilder parametersArray = provider.createArrayBuilder();
            for (AppParameter parameter : buildParameters) {
                parametersArray.add(parameter.getJSON());
            }
            function.add(AppJsonConstant.APP_FUNCTION_BUILD_PARAMETERS, parametersArray);
        }
        if (!runParameters.isEmpty()) {
            JsonArrayBuilder parametersArray = provider.createArrayBuilder();
            for (AppParameter parameter : runParameters) {
                parametersArray.add(parameter.getJSON());
            }
            function.add(AppJsonConstant.APP_FUNCTION_RUN_PARAMETERS, parametersArray);
        }
        return function.build().toString();
    }
}
