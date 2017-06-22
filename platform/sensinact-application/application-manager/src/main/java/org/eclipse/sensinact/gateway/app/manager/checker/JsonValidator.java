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
package org.eclipse.sensinact.gateway.app.manager.checker;

import org.eclipse.sensinact.gateway.app.api.exception.FunctionNotFoundException;
import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.manager.osgi.PluginsProxy;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class validates the JSON provided by the client to deploy an application
 *
 * @author Remi Druilhe
 */
public class JsonValidator {

    private static final String JSON_SCHEMA = "application.json";

    /**
     * Validate a JSON application against the JSON Schema for the applications
     * @param mediator the mediator to print output
     * @param json the JSON to validate
     * @throws ValidationException
     * @throws FileNotFoundException
     */
    public static void validateApplication(AppServiceMediator mediator, JSONObject json)
            throws ValidationException, FileNotFoundException {
        JSONObject rawSchema = null;

        try {
            rawSchema = new JSONObject(new JSONTokener(new InputStreamReader(mediator.getContext().getBundle()
                    .getResource("/" + JSON_SCHEMA).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(rawSchema == null) {
            throw new FileNotFoundException("Unable to find the JSON schema file");
        }

        Schema schema = SchemaLoader.load(rawSchema);
        schema.validate(json);
    }

    /**
     * Validate the application components against there JSON Schema in the plugins
     * @param mediator the mediator to print output
     * @param components the components to validate
     * @throws ValidationException
     */
    public static void validateFunctionsParameters(AppServiceMediator mediator, JSONArray components)
            throws ValidationException, InvalidApplicationException {

        for(int i = 0; i < components.length(); i++) {
            String function = components.getJSONObject(i).getJSONObject("function").getString("name");
            JSONObject functionSchema;

            try {
                functionSchema = PluginsProxy.getComponentJSONSchema(mediator, function);
            } catch (FunctionNotFoundException e) {
                if(mediator.isErrorLoggable()) {
                    mediator.error(e.getMessage(), e);
                }

                return;
            }

            if (functionSchema == null) {
                if(mediator.isErrorLoggable()) {
                    mediator.error("The JSON of the application is not valid.");
                }

                throw new InvalidApplicationException("Unable to find the JSON schema of the function: " + function);
            }

            JSONObject reformatedFunction = new JSONObject();
            reformatedFunction.put("name", function);

            if(components.getJSONObject(i).getJSONObject("function").has("buildparameters")) {
                reformatedFunction.put("buildparameters", components.getJSONObject(i).getJSONObject("function")
                        .getJSONArray("buildparameters"));
            }

            reformatedFunction.put("runparameters", components.getJSONObject(i).getJSONObject("function")
                    .getJSONArray("runparameters"));

            Schema schema = SchemaLoader.load(functionSchema);

            try {
                schema.validate(reformatedFunction);
            } catch (ValidationException e) {
                if(mediator.isDebugLoggable()) {
                    mediator.debug("The JSON of the component \""
                            + components.getJSONObject(i).getString("identifier") + "\" is not valid");
                }

                throw new ValidationException(schema,
                        "The JSON of the component \"" + components.getJSONObject(i).getString("identifier")
                                + "\" is not valid",
                        e.getKeyword());
            }
        }
    }
}
