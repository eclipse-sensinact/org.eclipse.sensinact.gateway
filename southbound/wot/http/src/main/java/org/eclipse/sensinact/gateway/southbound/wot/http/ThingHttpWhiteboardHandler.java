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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

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
import com.fasterxml.jackson.core.type.TypeReference;
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
     * DTO mapper
     */
    private final ObjectMapper mapper = new ObjectMapper();

    public ThingHttpWhiteboardHandler(final Thing thing, final HttpClient client) {
        this.thing = thing;
        this.client = client;
    }

    @Override
    public Promise<Object> act(PromiseFactory pf, String modelPackageUri, String model, String provider, String service,
            String resource, Map<String, Object> arguments) {

        ActionAffordance action = thing.actions.get(resource);
        if (action == null) {
            logger.error("Action {} not found for thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("Action not found"));
        }

        // Find the URL to invoke the action
        Optional<Form> opt = action.forms.stream()
                .filter(f -> f.op.contains(Operations.INVOKE_ACTION) && f.href.toLowerCase().startsWith("http"))
                .findFirst();
        if (opt.isEmpty()) {
            logger.error("No form found to invoke action {} on thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("No form found to invoke action"));
        }

        final Form form = opt.get();
        final URI uri;
        try {
            uri = new URI(form.href);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI for action {} on thing {}: {}", resource, thing.id, form.href);
            return pf.failed(new IllegalArgumentException("Invalid URI"));
        }

        // Fill in arguments with default / const values
        Map<String, Object> content;
        if (action.input != null) {
            content = new HashMap<>();
            content.put("input", convertArguments(action.input, arguments));
        } else {
            content = Map.of();
        }

        // Prepare the request
        Request request = client.newRequest(uri);

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

                try {
                    final Map<String, Object> response = mapper.readValue(getContent(),
                            new TypeReference<Map<String, Object>>() {
                            });

                    if (response.containsKey("result")) {
                        promise.resolve(mapper.convertValue(response.get("result"), resultClass));
                    } else if (response.containsKey("error")) {
                        promise.fail(new Exception("Error invoking action: " + response.get("error")));
                    } else {
                        promise.fail(new Exception("Unsupported result format"));
                    }
                } catch (IOException e) {
                    logger.error("Failed to parse response from action {} on thing {}", resource, thing.id, e);
                    promise.fail(e);
                }
            }
        });
        return promise.getPromise();
    }

    @SuppressWarnings("unchecked")
    private Object convertArguments(DataSchema schema, Map<String, Object> arguments) {
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

        PropertyAffordance property = thing.properties.get(resource);
        if (property == null) {
            logger.error("Property {} not found for thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("Property not found"));
        }

        // Find the URL to invoke the property
        Optional<Form> opt = property.forms.stream()
                .filter(f -> f.op.contains(Operations.WRITE_PROPERTY) && f.href.toLowerCase().startsWith("http"))
                .findFirst();
        if (opt.isEmpty()) {
            logger.error("No form found to read property {} on thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("No form found to read property"));
        }

        final Class<?> resultClass = Utils.classFromType(property.schema.type);
        if (newValue.getValue() != null && !resultClass.isAssignableFrom(newValue.getValue().getClass())) {
            logger.error("Invalid value type for property {} on thing {}", resource, thing.id);
            return pf.failed(
                    new ClassCastException("Expected " + resultClass + ", got " + newValue.getValue().getClass()));
        }

        final Form form = opt.get();
        final URI uri;
        try {
            uri = new URI(form.href);
        } catch (URISyntaxException e) {
            logger.error("Invalid URI for property {} on thing {}: {}", resource, thing.id, form.href);
            return pf.failed(e);
        }

        // Prepare the request
        Request request = client.newRequest(uri);

        String method = (String) form.getAdditionalProperties().get("htv:methodName");
        if (method == null) {
            method = "PUT";
        }
        request.method(method);

        final Map<String, Object> body = new HashMap<>();
        body.put("value", newValue.getValue());
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

    @Override
    public Promise<TimedValue<Object>> pullValue(PromiseFactory pf, String modelPackageUri, String model,
            String provider, String service, String resource, Class<Object> resourceType,
            TimedValue<Object> cachedValue) {

        PropertyAffordance property = thing.properties.get(resource);
        if (property == null) {
            logger.error("Property {} not found for thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("Property not found"));
        }

        // Find the URL to invoke the property
        Optional<Form> opt = property.forms.stream()
                .filter(f -> f.op.contains(Operations.READ_PROPERTY) && f.href.toLowerCase().startsWith("http"))
                .findFirst();
        if (opt.isEmpty()) {
            logger.error("No form found to read property {} on thing {}", resource, thing.id);
            return pf.failed(new IllegalArgumentException("No form found to read property"));
        }

        final Form form = opt.get();
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
                try {
                    final Map<String, Object> response = mapper.readValue(responseContent,
                            new TypeReference<Map<String, Object>>() {
                            });

                    if (response.containsKey("value")) {
                        final Instant timestamp = Instant.now();
                        final Object value = mapper.convertValue(response.get("value"), resultClass);
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
                    } else {
                        promise.fail(new Exception("Unsupported result format"));
                    }
                } catch (IOException e) {
                    logger.error("Failed to parse response from property {} on thing {}", resource, thing.id, e);
                    logger.debug("Response content from {}:\n{}", thing.id, responseContent);
                    promise.fail(e);
                }
            }
        });
        return promise.getPromise();
    }
}
