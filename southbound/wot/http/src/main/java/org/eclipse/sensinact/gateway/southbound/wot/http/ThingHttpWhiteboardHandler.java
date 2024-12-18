/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - Initial contribution
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringRequestContent;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.whiteboard.WhiteboardAct;
import org.eclipse.sensinact.core.whiteboard.WhiteboardSet;
import org.eclipse.sensinact.gateway.southbound.wot.api.ActionAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Form;
import org.eclipse.sensinact.gateway.southbound.wot.api.Operations;
import org.eclipse.sensinact.gateway.southbound.wot.api.PropertyAffordance;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.constants.Utils;
import org.eclipse.sensinact.gateway.southbound.wot.api.constants.WoTConstants;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.DataSchema;
import org.eclipse.sensinact.gateway.southbound.wot.api.dataschema.ObjectSchema;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingHttpWhiteboardHandler implements WhiteboardSet<Object>, WhiteboardAct<Object> {

    private final static Logger logger = LoggerFactory.getLogger(ThingHttpWhiteboardHandler.class);

    /**
     * Associated thing
     */
    private final Thing thing;

    /**
     * Shared HTTP client
     */
    private final HttpClient client;

    /**
     * Handler configuration
     */
    private final WhiteboardHandlerConfiguration config;

    /**
     * Per-URL prefix configuration
     */
    private final Map<String, WhiteboardHandlerConfiguration> perUrlConfig;

    /**
     * DTO mapper
     */
    private final ObjectMapper mapper = new ObjectMapper();

    public ThingHttpWhiteboardHandler(final Thing thing, final HttpClient client,
            final WhiteboardHandlerConfiguration mainConfig,
            final Map<String, WhiteboardHandlerConfiguration> perUrlConfig) {
        this.thing = thing;
        this.client = client;
        this.config = mainConfig;
        this.perUrlConfig = perUrlConfig;
    }

    /**
     * Look for the configuration matching the target URI
     *
     * @param targetUri Target URI
     * @return The configuration matching the URI prefix or the main configuration
     */
    private WhiteboardHandlerConfiguration findConfiguration(final URI targetUri) {
        final String strTarget = targetUri.toString();

        String bestMatch = null;
        WhiteboardHandlerConfiguration bestConfig = this.config;
        for (Entry<String, WhiteboardHandlerConfiguration> e : perUrlConfig.entrySet()) {
            final String url = e.getKey();
            if (strTarget.startsWith(url)) {
                if (bestMatch == null || url.length() > bestMatch.length()) {
                    bestMatch = url;
                    bestConfig = this.config;
                }
            }
        }
        return bestConfig;
    }

    /**
     * Looks for the first matching form
     *
     * @param forms      List of forms
     * @param operation  Operation to look for
     * @param allowEmpty Consider forms without explicit operations as matching
     * @return
     */
    private Form findForm(final List<Form> forms, final String operation, final boolean allowEmpty) {
        if (forms == null || forms.isEmpty()) {
            return null;
        }

        final List<Form> httpForms = forms.stream().filter(f -> f.href.toLowerCase().startsWith("http"))
                .collect(Collectors.toList());
        if (httpForms.isEmpty()) {
            return null;
        } else {
            return httpForms.stream().filter(
                    f -> (allowEmpty && f.op == null || f.op.isEmpty()) || (f.op != null && f.op.contains(operation)))
                    .findFirst().orElse(null);
        }
    }

    @Override
    public Promise<Object> act(PromiseFactory pf, String modelPackageUri, String model, String provider, String service,
            String resource, Map<String, Object> arguments) {

        final ActionAffordance action = thing.actions.get(resource);
        if (action == null) {
            logger.error("Action {} not found for thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("Action not found"));
        }

        // Find the URL to invoke the action
        final Form form = findForm(action.forms, Operations.INVOKE_ACTION, true);
        if (form == null) {
            logger.error("No form found to invoke action {} on thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("No form found to invoke action"));
        }

        final URI uri;
        try {
            uri = new URI(form.href);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI for action {} on thing {}: {}", resource, thing.id, form.href);
            return pf.failed(new IllegalArgumentException("Invalid URI"));
        }

        // Fill in arguments with default / const values
        final Object content = prepareArgumentsPayload(uri, action.input, arguments);

        // Prepare the request
        final Request request = client.newRequest(uri);

        String method = (String) form.getAdditionalProperties().get("htv:methodName");
        if (method == null) {
            method = "POST";
        }
        request.method(method);

        try {
            request.body(new StringRequestContent(MimeTypes.Type.APPLICATION_JSON.asString(),
                    mapper.writeValueAsString(content)));
        } catch (JsonProcessingException e) {
            logger.error("Invalid value for property {} on thing {}", resource, thing.id);
            return pf.failed(e);
        }

        final Class<?> resultClass = Utils.classFromType(action.output.type);

        final Deferred<Object> promise = pf.deferred();
        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                if (result.isFailed()) {
                    logger.error("Failed to invoke action {} on thing {}", resource, thing.id, result.getFailure());
                    promise.fail(result.getFailure());
                    return;
                }

                final int status = result.getResponse().getStatus();
                if (status >= 300) {
                    logger.error("Error querying HTTP endpoint for {}/{}/{}: {} ({})", provider, service, resource,
                            status, uri);
                    promise.fail(new IOException("HTTP error " + status));
                    return;
                }

                try {
                    promise.resolve(findValue(uri, action.output, resultClass, getContentAsString()));
                } catch (IOException e) {
                    logger.error("Failed to parse response from action {} on thing {}", resource, thing.id, e);
                    promise.fail(e);
                }
            }
        });
        return promise.getPromise();
    }

    /**
     * Prepare the payload to send to an action
     *
     * @param targetUri Target URI
     * @param schema    Action input schema
     * @param arguments Action arguments received by sensiNact
     * @return The payload to send to the server
     */
    private Object prepareArgumentsPayload(final URI targetUri, final DataSchema schema,
            final Map<String, Object> arguments) {
        if (schema == null) {
            return null;
        }

        final WhiteboardHandlerConfiguration config = findConfiguration(targetUri);
        final Object convertedArgs = convertArguments(schema, arguments);
        if (config.argumentsKey != null) {
            // Arguments must be in a map with the given key
            if (convertedArgs != null) {
                return Map.of(config.argumentsKey, convertedArgs);
            } else if (config.useArgumentsKeyOnEmptyArgs) {
                final Map<String, Object> mapArgs = new HashMap<>();
                mapArgs.put(config.argumentsKey, null);
                return mapArgs;
            } else {
                return null;
            }
        }

        // Return the arguments as is
        return convertedArgs;
    }

    @SuppressWarnings("unchecked")
    private Object convertArguments(final DataSchema schema, final Map<String, Object> arguments) {
        if (schema == null) {
            // Not enough information to convert arguments, return them as is
            return arguments;
        }

        if (schema.constantValue != null) {
            return schema.constantValue;
        }

        if (arguments == null) {
            return schema.defaultValue;
        }

        switch (schema.type) {
        case "object": {
            Map<String, Object> newArgs = new HashMap<>();
            for (Entry<String, DataSchema> propEntry : ((ObjectSchema) schema).properties.entrySet()) {
                String argName = propEntry.getKey();
                DataSchema argSchema = propEntry.getValue();

                if (argSchema.constantValue != null) {
                    newArgs.put(argName, argSchema.constantValue);
                } else if ("object".equals(argSchema.type)) {
                    newArgs.put(argName, convertArguments(argSchema, (Map<String, Object>) arguments.get(argName)));
                } else {
                    newArgs.put(argName, arguments.getOrDefault(argName, argSchema.defaultValue));
                }
            }
            return newArgs;
        }

        default:
            if (arguments.size() == 1) {
                // 1 argument given, use it as is
                return arguments.values().iterator().next();
            } else {
                return arguments.getOrDefault(WoTConstants.DEFAULT_ARG_NAME, schema.defaultValue);
            }
        }
    }

    @Override
    public Promise<TimedValue<Object>> pushValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<Object> cachedValue, TimedValue<Object> newValue) {

        final PropertyAffordance property = thing.properties.get(resource);
        if (property == null) {
            logger.error("Property {} not found for thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("Property not found"));
        }

        final Class<?> resultClass = Utils.classFromType(property.schema.type);
        if (newValue.getValue() != null && !resultClass.isAssignableFrom(newValue.getValue().getClass())) {
            logger.error("Invalid value type for property {} on thing {}", resource, thing.id);
            return pf.failed(
                    new ClassCastException("Expected " + resultClass + ", got " + newValue.getValue().getClass()));
        }

        // Find the URL to invoke the property
        final Form form = findForm(property.forms, Operations.WRITE_PROPERTY, false);
        if (form == null) {
            logger.error("No form found to read property {} on thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("No form found to read property"));
        }

        final URI uri;
        try {
            uri = new URI(form.href);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI for property {} on thing {}: {}", resource, thing.id, form.href);
            return pf.failed(e);
        }

        // Prepare the request
        final Request request = client.newRequest(uri);

        String method = (String) form.getAdditionalProperties().get("htv:methodName");
        if (method == null) {
            method = "PUT";
        }
        request.method(method);

        final Object body = preparePropertyPayload(uri, newValue);
        try {
            request.body(new StringRequestContent(MimeTypes.Type.APPLICATION_JSON.asString(),
                    mapper.writeValueAsString(body)));
        } catch (JsonProcessingException e) {
            logger.error("Invalid value for property {} on thing {}", resource, thing.id);
            return pf.failed(e);
        }

        final Deferred<TimedValue<Object>> promise = pf.deferred();
        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                if (result.isFailed()) {
                    logger.error("Failed to update property {} on thing {}", resource, thing.id, result.getFailure());
                    promise.fail(result.getFailure());
                    return;
                }

                final int status = result.getResponse().getStatus();
                if (status < 300) {
                    // Success!
                    promise.resolve(newValue);
                } else {
                    promise.fail(new Exception("Error " + status + " updating resource"));
                }
            }
        });
        return promise.getPromise();
    }

    /**
     * Prepare the content of a property write payload
     *
     * @param targetUri Target URI
     * @param newValue  New property value
     * @return The content to send to the server
     */
    private Object preparePropertyPayload(final URI targetUri, final TimedValue<?> newValue) {
        final WhiteboardHandlerConfiguration config = findConfiguration(targetUri);
        if (config.propertyKey != null) {
            final Map<String, Object> body = new HashMap<>();
            body.put(config.propertyKey, newValue.getValue());
            if (config.timestampKey != null) {
                // Add a timestamp key
                body.put(config.timestampKey, newValue.getTimestamp().toString());
            }
            return body;
        } else {
            // Use the value as is
            return newValue.getValue();
        }
    }

    @Override
    public Promise<TimedValue<Object>> pullValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<Object> cachedValue) {

        final PropertyAffordance property = thing.properties.get(resource);
        if (property == null) {
            logger.error("Property {} not found for thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("Property not found"));
        }

        // Find the URL to invoke the property
        final Form form = findForm(property.forms, Operations.READ_PROPERTY, true);
        if (form == null) {
            logger.error("No form found to read property {} on thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("No form found to read property"));
        }

        final URI uri;
        try {
            uri = new URI(form.href);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI for property {} on thing {}: {}", resource, thing.id, form.href);
            return pf.failed(new IllegalArgumentException("Invalid URI"));
        }

        // Prepare the request
        Request request = client.newRequest(uri);

        String method = (String) form.getAdditionalProperties().get("htv:methodName");
        if (method == null) {
            method = "GET";
        }
        request.method(method);

        final Class<?> resultClass = Utils.classFromType(property.schema.type);

        final Deferred<TimedValue<Object>> promise = pf.deferred();
        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                if (result.isFailed()) {
                    logger.error("Failed to invoke property {} on thing {}", resource, thing.id, result.getFailure());
                    promise.fail(result.getFailure());
                    return;
                }

                final String responseContent = getContentAsString();
                final int status = result.getResponse().getStatus();
                if (status >= 300) {
                    logger.error("Error querying HTTP endpoint for {}/{}/{}: {} ({})", provider, service, resource,
                            status, uri);
                    promise.fail(new IOException("HTTP error " + status));
                    return;
                }

                try {
                    final Instant timestamp = Instant.now();
                    final Object value = findValue(uri, property.schema, resultClass, responseContent);
                    promise.resolve(new TimedValue<Object>() {
                        @Override
                        public Instant getTimestamp() {
                            return timestamp;
                        }

                        @Override
                        public Object getValue() {
                            return value;
                        }
                    });
                } catch (IOException e) {
                    logger.error("Failed to parse response from property {} on thing {}", resource, thing.id, e);
                    logger.debug("Response content from {}:\n{}", thing.id, responseContent);
                    promise.fail(e);
                }
            }
        });
        return promise.getPromise();
    }

    /**
     * Look for the result value. Check if is it buried in a result map
     *
     * @param targetUri   Target URI
     * @param schema      Output schema
     * @param resultClass Expected result class
     * @param strResponse Endpoint response as a string
     * @return The found value
     * @throws JsonMappingException    Error parsing response
     * @throws JsonProcessingException Error parsing response
     */
    private Object findValue(final URI targetUri, final DataSchema schema, final Class<?> resultClass,
            final String strResponse) throws JsonMappingException, JsonProcessingException {
        final Object rawResponse = mapper.readValue(strResponse, Object.class);
        if (rawResponse == null) {
            return null;
        }

        if (!schema.type.equals("object") && rawResponse instanceof Map) {
            // We might have a step
            final Map<?, ?> mapResponse = (Map<?, ?>) rawResponse;

            final WhiteboardHandlerConfiguration config = findConfiguration(targetUri);
            if (config.propertyKey != null && mapResponse.containsKey(config.propertyKey)) {
                return mapResponse.get(config.propertyKey);
            }

            for (String key : List.of("value", "result", "answer")) {
                if (mapResponse.containsKey(key)) {
                    logger.debug("Got data through intermediate key {} -> {}", key, mapResponse.get(key));
                    return mapResponse.get(key);
                }
            }
        }
        return rawResponse;
    }
}
