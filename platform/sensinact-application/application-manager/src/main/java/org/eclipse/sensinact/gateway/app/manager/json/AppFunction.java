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
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONArray;
import org.json.JSONObject;

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

    public AppFunction(JSONObject json) {
        this.name = json.getString(AppJsonConstant.APP_FUNCTION_NAME);
        this.buildParameters = new ArrayList<AppParameter>();
        if (json.has(AppJsonConstant.APP_FUNCTION_BUILD_PARAMETERS)) {
            JSONArray parameterArray = json.getJSONArray(AppJsonConstant.APP_FUNCTION_BUILD_PARAMETERS);
            for (int i = 0; i < parameterArray.length(); i++) {
                buildParameters.add(new AppParameter(parameterArray.getJSONObject(i)));
            }
        }
        this.runParameters = new ArrayList<AppParameter>();
        if (json.has(AppJsonConstant.APP_FUNCTION_RUN_PARAMETERS)) {
            JSONArray parameterArray = json.getJSONArray(AppJsonConstant.APP_FUNCTION_RUN_PARAMETERS);
            for (int i = 0; i < parameterArray.length(); i++) {
                runParameters.add(new AppParameter(parameterArray.getJSONObject(i)));
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
        JSONObject function = new JSONObject();
        function.put(AppJsonConstant.APP_FUNCTION_NAME, name);
        if (!buildParameters.isEmpty()) {
            JSONArray parametersArray = new JSONArray();
            for (AppParameter parameter : buildParameters) {
                parametersArray.put(parameter.getJSON());
            }
            function.put(AppJsonConstant.APP_FUNCTION_BUILD_PARAMETERS, parametersArray);
        }
        if (!runParameters.isEmpty()) {
            JSONArray parametersArray = new JSONArray();
            for (AppParameter parameter : runParameters) {
                parametersArray.put(parameter.getJSON());
            }
            function.put(AppJsonConstant.APP_FUNCTION_RUN_PARAMETERS, parametersArray);
        }
        return function.toString();
    }
}
