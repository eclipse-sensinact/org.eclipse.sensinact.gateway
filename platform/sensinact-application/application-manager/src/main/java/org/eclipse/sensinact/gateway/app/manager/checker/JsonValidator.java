/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.checker;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.sensinact.gateway.app.api.exception.FunctionNotFoundException;
import org.eclipse.sensinact.gateway.app.api.exception.ValidationException;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.app.manager.osgi.PluginsProxy;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * This class validates the JSON provided by the client to deploy an application
 *
 * @author Remi Druilhe
 */
public class JsonValidator {
	
	private static final Logger LOG = LoggerFactory.getLogger(JsonValidator.class);
    private static final String JSON_SCHEMA = "application.json";

    /**
     * Validate a JSON application against the JSON Schema for the applications
     *
     * @param mediator the mediator to print output
     * @param json     the JSON to validate
     * @throws ValidationException   JSON file is not valid
     * @throws FileNotFoundException unable to find the JSON schema
     */
    public static void validateApplication(AppServiceMediator mediator, JsonObject json) throws ValidationException, FileNotFoundException {
        JsonObject rawSchema = null;
        try {
            rawSchema = JsonProviderFactory.getProvider().createReader(mediator.getContext().getBundle().getResource("/" + JSON_SCHEMA).openStream()).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (rawSchema == null) {
            throw new FileNotFoundException("Unable to find the JSON schema file");
        }
        /*Json inputJson = Json.read(json.toString());
        Json schemaJson = Json.read(mediator.getContext().getBundle().getResource("/" + JSON_SCHEMA));
        Json.Schema schema = Json.schema(schemaJson);
        Json errors = schema.validate(inputJson);
        if (!errors.at("ok").asBoolean()) {
            throw new ValidationException("Validation error " + errors.toString());
        }*/
    }

    /**
     * Validate the application components against there JSON Schema in the plugins
     *
     * @param mediator   the mediator to print output
     * @param components the components to validate
     * @throws ValidationException   JSON file is not valid
     * @throws FileNotFoundException unable to find the JSON schema
     */
    public static void validateFunctionsParameters(AppServiceMediator mediator, JsonArray components) throws ValidationException, FileNotFoundException {
    	for (int i = 0; i < components.size(); i++) {
    		String function = components.getJsonObject(i).getJsonObject("function").getString("name");
    		JsonObject functionSchema;
    		try {
    			functionSchema = PluginsProxy.getComponentJsonSchema(mediator, function);
    		} catch (FunctionNotFoundException e) {
    			if (LOG.isErrorEnabled()) {
    				LOG.error(e.getMessage(), e);
    			}
    			return;
    		}
    		if (functionSchema == null) {
    			if (LOG.isErrorEnabled()) {
    				LOG.error("The JSON of the application is not valid.");
    			}
    			throw new FileNotFoundException("Unable to find the JSON schema of the function: " + function);
    		}
    		JSONObject reformatedFunction = new JSONObject();
    		reformatedFunction.put("name", function);
    		if (components.getJsonObject(i).getJsonObject("function").containsKey("buildparameters")) {
    			reformatedFunction.put("buildparameters", components.getJsonObject(i).getJsonObject("function").getJsonArray("buildparameters"));
    		}
    		reformatedFunction.put("runparameters", components.getJsonObject(i).getJsonObject("function").getJsonArray("runparameters"));
            //Schema schema = SchemaLoader.load(functionSchema);
            /*Json inputJson = Json.read(reformatedFunction.toString());
            Json schemaJson = Json.read(functionSchema.toString());
            Json.Schema schema = Json.schema(schemaJson);
            Json errors = schema.validate(inputJson);
            if (!errors.at("ok").asBoolean()) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("The JSON of the component \""
                            + components.getJSONObject(i).getString("identifier") + "\" is not valid");
                }
                throw new ValidationException("The JSON of the component \"" + components.getJSONObject(i).getString("identifier")
                        + "\" is not valid: " + errors.toString());
            }*/
            /*try {
                schema.validate(reformatedFunction);
            } catch (ValidationException e) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("The JSON of the component \""
                            + components.getJSONObject(i).getString("identifier") + "\" is not valid");
                }
                throw new ValidationException(schema,
                        "The JSON of the component \"" + components.getJSONObject(i).getString("identifier")
                                + "\" is not valid",
                        e.getKeyword());
            }*/
        }
    }
}
