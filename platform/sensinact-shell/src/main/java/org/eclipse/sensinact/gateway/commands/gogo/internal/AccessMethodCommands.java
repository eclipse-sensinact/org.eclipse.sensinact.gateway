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
package org.eclipse.sensinact.gateway.commands.gogo.internal;

import org.apache.felix.service.command.Descriptor;
import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.commands.gogo.osgi.CommandServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceDescription;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;

public class AccessMethodCommands {

    private CommandServiceMediator mediator;

    private final String _line;
    private final String _tab;

    public AccessMethodCommands(CommandServiceMediator mediator) {
        this.mediator = mediator;
        this._line = ShellUtils.lineSeparator(100);
        this._tab = ShellUtils.tabSeparator(3);
    }

    /**
     * Get the description of a specific method of a resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     * @param methodType
     */
    @Descriptor("get the description of a specific method of a resource of a sensiNact service")
    public void method(@Descriptor("the service provider ID") String serviceProviderID,
                       @Descriptor("the service ID") String serviceID,
                       @Descriptor("the resource ID") String resourceID,
                       @Descriptor("the method type") String methodType) {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getServiceProvider(serviceProviderID).getService(serviceID).getResource(resourceID);

        if (resource != null) {
            JSONArray methods = new JSONObject(resource.getDescription().getDescription()).getJSONArray("accessMethods");

            if(methods.length() != 0) {
                buffer.append("\n" + _line);

                for(int i = 0; i < methods.length(); i++) {
                    if(methods.getJSONObject(i).getString("name").equalsIgnoreCase(methodType)){
                        JSONArray parameters = methods.getJSONObject(i).getJSONArray("parameters");

                        buffer.append("\n" + _tab + methodType.toUpperCase() + ": [");

                        for(int j = 0; j < parameters.length(); j++) {
                            buffer.append("(name: " + parameters.getJSONObject(j).getString("name"));
                            buffer.append(" | type: " + parameters.getJSONObject(j).getString("type") + ")");
                        }

                        buffer.append("]");
                    }
                }

                buffer.append("\n" + _line + "\n");
            } else {
                buffer.append("Method '" + methodType + "' for the resource '" + resource.getName()
                        + "' of the sensiNact service instance '" + serviceID + "' not found");
            }
        } else {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }

        System.out.println(buffer.toString());
    }

    /**
     * Get the value of the default attribute resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     */
    @Descriptor("get the value of the default attribute resource of a sensiNact service")
    public void get(@Descriptor("the service provider ID") String serviceProviderID,
                    @Descriptor("the service ID") String serviceID,
                    @Descriptor("the resource ID") String resourceID)
    {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getResource(serviceProviderID, serviceID, resourceID);

        if(resource != null)
        {
        	GetResponse response = resource.get(DataResource.VALUE);
        	
            buffer.append("\n" + _line);
            buffer.append("\n" + _tab + "Value: " + response.getResponse(DataResource.VALUE));
            buffer.append("\n" + _tab + "Type: " + response.getResponse(DataResource.TYPE));
            buffer.append("\n" + _line + "\n");
            
        } else
        {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }

        System.out.println(buffer.toString());
    }

    /**
     * Get the value of a specific attribute resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     * @param attributeID the ID of the attribute
     */
    @Descriptor("get the value of a specific attribute resource of a sensiNact service")
    public void get(@Descriptor("the service provider ID") String serviceProviderID,
                    @Descriptor("the service ID") String serviceID,
                    @Descriptor("the resource ID") String resourceID,
                    @Descriptor("the attribute ID") String attributeID)
    {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getResource(serviceProviderID, serviceID, resourceID);

        if (resource != null)
        {
            Description attribute = resource.<ResourceDescription>getDescription(
            		).element(attributeID);

            if (attribute != null)
            {
            	GetResponse response = resource.get(attributeID);            	
                buffer.append("\n" + _line);
                buffer.append("\n" + _tab + "Name: " + response.getResponse(DataResource.NAME));
                buffer.append("\n" + _tab + "Value: " + response.getResponse(DataResource.VALUE));
                buffer.append("\n" + _tab + "Type: " + response.getResponse(DataResource.TYPE));
                buffer.append("\n" + _line + "\n");
                
            } else {
                buffer.append("Attribute '" + attributeID + "' of resource '" + resourceID
                        + "' for the sensiNact service instance '" + serviceID + "' not found");
            }
        } else {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }

        System.out.println(buffer.toString());
    }

    /**
     * Set a specific value to the default attribute resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     * @param value the value to set
     */
    @Descriptor("set a specific value to the default attribute resource of a sensiNact service")
    public void set(@Descriptor("the service provider ID") String serviceProviderID,
                    @Descriptor("the service ID") String serviceID,
                    @Descriptor("the resource ID") String resourceID,
                    @Descriptor("the resource value") Object value) 
    {
        this.set(serviceProviderID, serviceID, resourceID,DataResource.VALUE, value);
    }

    /**
     * Set a specific value to an attribute of a resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     * @param attributeID the ID of the attribute
     * @param value the value to set
     */
    @Descriptor("set a specific value to an attribute of a resource of a sensiNact service")
    public void set(@Descriptor("the service provider ID") String serviceProviderID,
                    @Descriptor("the service ID") String serviceID,
                    @Descriptor("the resource ID") String resourceID,
                    @Descriptor("the attribute ID") String attributeID,
                    @Descriptor("the resource value") Object value) 
    {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getResource(
        		serviceProviderID, serviceID, resourceID);

        if (resource != null)
        {
        	if(Modifiable.MODIFIABLE.equals(resource.<ResourceDescription>getDescription(
        			).element(attributeID).getModifiable()))
            {
                if (value != null) 
                {
                    SetResponse response = resource.set(attributeID, value);
                    buffer.append("\n" + _line);
                    buffer.append("\n" + _tab + "Name: " + response.getResponse(DataResource.NAME));
                    buffer.append("\n" + _tab + "Value: " + response.getResponse(DataResource.VALUE));
                    buffer.append("\n" + _tab + "Type: " + response.getResponse(DataResource.TYPE));
                    buffer.append("\n" + _line + "\n");
                    
                } else 
                {
                    buffer.append("Wrong parameters");
                }
            } else {
                buffer.append("Attribute '" + attributeID + "' is not modifiable");
            }
        } else 
        {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }
        System.out.println(buffer.toString());
    }

    /**
     * Execute a specific resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     */
    @Descriptor("execute a specific resource of a sensiNact service")
    public void act(@Descriptor("the service provider ID") String serviceProviderID,
                    @Descriptor("the service ID") String serviceID,
                    @Descriptor("the resource ID") String resourceID) 
    {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getResource(serviceProviderID, serviceID, resourceID);

        if(resource != null)
        {
            if (resource instanceof ActionResource) 
            {
                ActResponse response = ((ActionResource) resource).act();

                if(AccessMethodResponse.Status.SUCCESS.equals(response.getStatus()))
                {
                    buffer.append("Action success");
                    
                } else
                {
                    buffer.append("Action error");
                }
                if(response.getResponse("message") != null) 
                {
                    buffer.append(": " + response.getResponse("message"));
                }
            } else 
            {
                buffer.append("Resource '" + resource.getName() + "' is not an ActionResource\n");
            }
        } else 
        {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }
        System.out.println(buffer.toString());
    }

    /**
     * Execute a specific resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     * @param parameters the parameters of the ACT
     */
    @Descriptor("execute a specific resource of a sensiNact service")
    public void act(@Descriptor("the service provider ID") String serviceProviderID,
                    @Descriptor("the service ID") String serviceID,
                    @Descriptor("the resource ID") String resourceID,
                    Object... parameters)
    {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getResource(serviceProviderID, serviceID, resourceID);

        if(resource != null)
        {
            if (resource instanceof ActionResource)
            {
                if (parameters != null)
                {
                    ActResponse response = ((ActionResource) resource).act(parameters);

                    if(AccessMethodResponse.Status.SUCCESS.equals(response.getStatus())) 
                    {
                        buffer.append("Action success");
                        
                    } else 
                    {
                        buffer.append("Action error");
                    }

                    if(response.getResponse("message") != null)
                    {
                        buffer.append(": " + response.getResponse("message"));
                    }
                } else 
                {
                    buffer.append("Wrong parameters");
                }
            } else 
            {
                buffer.append("Resource '" + resource.getName() + "' is not an ActionResource\n");
            }
        } else
        {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }

        System.out.println(buffer.toString());
    }

    /**
     * Cancel a specific subscription to a resource of a sensiNact service
     * @param serviceProviderID the ID of the service provider
     * @param serviceID the ID of the service
     * @param resourceID the ID of the resource
     * @param subscriptionID the ID of the subscription
     */
    @Descriptor("cancel a specific subscription to a resource of a sensiNact service")
    public void unsubscribe(@Descriptor("the service provider ID") String serviceProviderID,
                            @Descriptor("the service ID") String serviceID,
                            @Descriptor("the resource ID") String resourceID,
                            @Descriptor("the subscription ID") String subscriptionID) 
    {
        StringBuilder buffer = new StringBuilder();
        Resource resource = mediator.getSession().getResource(serviceProviderID, serviceID, resourceID);

        if (resource != null) 
        {
            UnsubscribeResponse response = resource.unsubscribe(DataResource.VALUE, subscriptionID);

            if (response.getStatus().equals(AccessMethodResponse.Status.SUCCESS)) 
            {
                buffer.append("\n");
                buffer.append(_line);
                buffer.append(response.get("Unsubscription done"));
                buffer.append("\n" + _line + "\n");
                
            } else
            {
                buffer.append("\nUnable to unsubscribe to resource '" + resourceID + "'\n");
            }
        } else
        {
            buffer.append("sensiNact resource instance /" + serviceProviderID +
                    "/" + serviceID + "/" + resourceID + " not found");
        }
        System.out.println(buffer.toString());
    }
}
