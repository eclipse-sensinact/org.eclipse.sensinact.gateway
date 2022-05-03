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
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.JSONResponseFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    public void method(@Descriptor("the service provider ID") String serviceProviderID, @Descriptor("the service ID") String serviceID, @Descriptor("the resource ID") String resourceID, @Descriptor("the method type") final String methodType) throws InvalidCredentialException, JSONException, IOException {

        new ShellAccess(new ShellAccessRequest(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, false)))) {
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
                JSONObject result = new JSONResponseFormat().format(cap.getJSON());
                if (result == null) {
                    this.sendError(500, "Internal server error");
                    return false;
                }
                JSONArray methods = result.optJSONArray("accessMethods");
                if (!JSONObject.NULL.equals(methods) && methods.length() > 0) {
                    for (int i = 0; i < methods.length(); i++) {
                        JSONObject m = methods.getJSONObject(i);
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
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "GET")));
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
        JSONArray params = new JSONArray();
        if (attributeID != null) {
            params.put(new JSONObject().put("name", "attributeName").put("type", "string").put("value", attributeID));
        }
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "GET")).put("parameters", params));
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
        JSONArray params = new JSONArray();
        if (attributeID != null) {
            params.put(new JSONObject().put("name", "attributeName").put("type", "string").put("value", attributeID));
        }
        params.put(new JSONObject().put("name", "argument").put("type", "string").put("value", String.valueOf(value)));
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "SET")).put("parameters", params));
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
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "ACT")));
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
        JSONArray params = new JSONArray();
        int index = 0;
        int length = parameters == null ? 0 : parameters.length;
        for (; index < length; index++) {
            params.put(new JSONObject().put("name", "arg" + index).put("type", "string").put("value", String.valueOf(parameters[index])));
        }
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "ACT")).put("parameters", params));
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
        JSONArray params = new JSONArray();
        if (attributeID != null) {
            params.put(new JSONObject().put("name", "attributeName").put("type", "string").put("value", attributeID));
        }
        try {
            params.put(new JSONObject().put("name", "conditions").put("type", "object").put("value", new JSONObject(conditions)));

        } catch (JSONException e) {
            LOG.error("Unable to parse the conditions", e);

        }
        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "SUBSCRIBE")).put("parameters", params));
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
        JSONArray params = new JSONArray();
        params.put(new JSONObject().put("name", "subscriptionId").put("type", "string").put("value", subscriptionID));

        ShellAccess.proceed(component.getCommandMediator(), new JSONObject().put("uri", CommandServiceMediator.uri(serviceProviderID, serviceID, resourceID, "UNSUBSCRIBE")).put("parameters", params));
    }
}
