/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import java.io.IOException;
import java.io.StringReader;

import org.apache.felix.service.command.Descriptor;
import org.apache.felix.service.command.annotations.GogoCommand;
import org.eclipse.sensinact.gateway.commands.gogo.internal.shell.ShellAccess;
import org.eclipse.sensinact.gateway.commands.gogo.internal.shell.ShellAccessRequest;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.spi.JsonProvider;

@Component(service = AccessMethodCommands.class)
@GogoCommand(
		scope = "sna", 
		function = {"method", "get", "set", "act", "subscribe", "unsubscribe"}
	)
public class AccessMethodCommands {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccessMethodCommands.class);
    
	@Reference
	private CommandComponent component;

    /**
     * Get the description of a specific method of a resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param methodType
     * @throws IOException
     * @throws JSONException
     * @throws InvalidCredentialException
     */
    @Descriptor("get the description of a specific method of a resource of a sensiNact service")
    public void method(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the method type") final String methodType) throws InvalidCredentialException, JsonException, IOException {

        new ShellAccess(new ShellAccessRequest(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, false)).build())) {
            /**
             * @inheritDoc
             *
             * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess#
             * respond(org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator, org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder)
             */
            @Override
            protected boolean respond(NorthboundMediator mediator, NorthboundRequestBuilder builder) throws IOException {
                NorthboundRequest nthbndRequest = builder.build();
                if (nthbndRequest == null) {
                    this.sendError(500, "Internal server error");
                    return false;
                }
                AccessMethodResponse<?> cap = super.endpoint.execute(nthbndRequest);
                JsonObject result = JsonProviderFactory.getProvider().createReader(new StringReader(cap.getJSON())).readObject();
                if (result == null) {
                    this.sendError(500, "Internal server error");
                    return false;
                }
                JsonArray methods = result.getJsonArray("accessMethods");
                if (methods != null && methods.size() > 0) {
                    for (int i = 0; i < methods.size(); i++) {
                        JsonObject m = methods.getJsonObject(i);
                        if (m.getString("name").equalsIgnoreCase(methodType)) {
                            ((CommandServiceMediator) mediator).getOutput().output(m, 0);
                        }
                    }
                }
                return true;
            }

        }.proceed();
    }

    /**
     * Get the value of the default attribute resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     */
    @Descriptor("get the value of the default attribute resource of a sensiNact service")
    public void get(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID) {
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "GET")).build());
    }

    /**
     * Get the value of a specific attribute resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param attributeID       the ID of the attribute
     */
    @Descriptor("get the value of a specific attribute resource of a sensiNact service")
    public void get(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the attribute ID") String attributeID) {
        JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder params = provider.createArrayBuilder();
        if (attributeID != null) {
            params.add(provider.createObjectBuilder().add("name", "attributeName").add("type", "string")
            		.add("value", attributeID));
        }
        ShellAccess.proceed(component.getCommandMediator(), provider.createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "GET"))
        		.add("parameters", params).build());
    }

    /**
     * Set a specific value to the default attribute resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param value             the value to set
     */
    @Descriptor("set a specific value to the default attribute resource of a sensiNact service")
    public void set(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the resource value") Object value) {
        this.set(serviceProviderID, serviceID, resourceID, DataResource.VALUE, value);
    }

    /**
     * Set a specific value to an attribute of a resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param attributeID       the ID of the attribute
     * @param value             the value to set
     */
    @Descriptor("set a specific value to an attribute of a resource of a sensiNact service")
    public void set(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the attribute ID") String attributeID, @Descriptor("the resource value") Object value) {
    	JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder params = provider.createArrayBuilder();
        if (attributeID != null) {
            params.add(provider.createObjectBuilder().add("name", "attributeName").add("type", "string")
            		.add("value", attributeID));
        }
        params.add(provider.createObjectBuilder().add("name", "argument").add("type", "string")
        		.add("value", String.valueOf(value)));
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "SET"))
        		.add("parameters", params).build());
    }

    /**
     * Execute a specific resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     */
    @Descriptor("execute a specific resource of a sensiNact service")
    public void act(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID) {
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "ACT")).build());
    }

    /**
     * Execute a specific resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param parameters        the parameters of the ACT
     */
    @Descriptor("execute a specific resource of a sensiNact service")
    public void act(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, Object... parameters) {
    	JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder params = provider.createArrayBuilder();
        int length = parameters == null ? 0 : parameters.length;
        for (int index = 0; index < length; index++) {
            params.add(provider.createObjectBuilder().add("name", "arg" + index).add("type", "string")
            		.add("value", String.valueOf(parameters[index])));
        }
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "ACT"))
        		.add("parameters", params).build());
    }

    /**
     * Set a specific value to an attribute of a resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param attributeID       the ID of the attribute
     * @param value             the value to set
     */
    @Descriptor("subscribe to a specific attribute of a resource of a sensiNact service")
    public void subscribe(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the attribute ID") String attributeID, @Descriptor("the applying JSON formated conditions") String conditions) {
    	JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder params = provider.createArrayBuilder();
        if (attributeID != null) {
            params.add(provider.createObjectBuilder().add("name", "attributeName").add("type", "string")
            		.add("value", attributeID));
        }
        try {
            params.add(provider.createObjectBuilder().add("name", "conditions").add("type", "object")
            		.add("value", provider.createReader(new StringReader(conditions)).readObject()));

        } catch (JsonException e) {
            LOG.error("Unable to parse the conditions", e);

        }
        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "SUBSCRIBE"))
        		.add("parameters", params).build());
    }

    /**
     * Cancel a specific subscription to a resource of a sensiNact service
     *
     * @param serviceProviderID the ID of the service provider
     * @param serviceID         the ID of the service
     * @param resourceID        the ID of the resource
     * @param subscriptionID    the ID of the subscription
     */
    @Descriptor("cancel a specific subscription to a resource of a sensiNact service")
    public void unsubscribe(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the subscription ID") String subscriptionID) {
    	JsonProvider provider = JsonProviderFactory.getProvider();
		JsonArrayBuilder params = provider.createArrayBuilder();
        params.add(provider.createObjectBuilder().add("name", "subscriptionId").add("type", "string")
        		.add("value", subscriptionID));

        ShellAccess.proceed(component.getCommandMediator(), JsonProviderFactory.getProvider().createObjectBuilder()
        		.add("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "UNSUBSCRIBE"))
        		.add("parameters", params).build());
    }
}
