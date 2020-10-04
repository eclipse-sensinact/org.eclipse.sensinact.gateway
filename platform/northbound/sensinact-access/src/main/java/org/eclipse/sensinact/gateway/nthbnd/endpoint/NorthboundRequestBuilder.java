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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.FilteringCollection;
import org.eclipse.sensinact.gateway.core.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;

/**
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class NorthboundRequestBuilder {
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

    protected NorthboundMediator mediator;

    protected String serviceProvider;
    protected String service;
    protected String resource;
    protected String attribute;
    protected String requestIdentifier;
    protected String method;
    protected boolean listElements;
    private Object argument;
    private FilteringDefinition[] filterDefinitions;
    private boolean hiddenFilter;

    /**
     * Constructor
     *
     * @param mediator the {@link NorthboundMediator} allowing
     *                 the NorthboundRequestBuilder to be instantiated to interact
     *                 with the OSGi host environment
     */
    public NorthboundRequestBuilder(NorthboundMediator mediator) {
        this.mediator = mediator;
        if (this.mediator == null) {
            throw new NullPointerException("Mediator needed");
        }
    }

    /**
     * Defines the String identifier of the service provider
     * targeted by the request to be built
     *
     * @param serviceProvider the String identifier of the
     *                        targeted service provider
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withServiceProvider(String serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    /**
     * Defines the String identifier of the service targeted
     * by the request to be built
     *
     * @param service the String identifier of the targeted
     *                service
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withService(String service) {
        this.service = service;
        return this;
    }

    /**
     * Defines the String identifier of the resource targeted
     * by the request to be built
     *
     * @param resource the String identifier of the
     *                 targeted resource
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * Defines the String identifier of the attribute targeted
     * by the request to be built
     *
     * @param attribute the String identifier of the
     *                  targeted attribute
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    /**
     * Defines the String identifier of the method to be executed
     * by the request to be built
     *
     * @param method the String identifier of the method to be
     *               executed
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Returns the String identifier of the method to be executed
     * by the request to be built
     *
     * @return the String identifier of the method to be
     * executed
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Define the argument that will be used to parameterize
     * the call to the method the request to be built is
     * targeting
     *
     * @param argument
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withArgument(Object argument) {
        this.argument = argument;
        return this;
    }

    /**
     * @param rid
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withRequestId(String rid) {
        this.requestIdentifier = rid;
        return this;
    }

    /**
     * @return
     */
    public String getRequestIdentifier() {
        return this.requestIdentifier;
    }

    /**
     * @param listElements
     * @return
     */
    public NorthboundRequestBuilder isElementsList(boolean listElements) {
        this.listElements = listElements;
        return this;
    }

    /**
     * @param size
     */
    public void withFilter(int size) {
        this.filterDefinitions = new FilteringDefinition[size];
    }

    /**
     * @param filterDefinition
     * @param f
     */
    public void withFilter(FilteringDefinition filterDefinition, int index) {
        if (filterDefinition == null || this.filterDefinitions == null) {
            return;
        }
        this.filterDefinitions[index] = filterDefinition;
    }

    /**
     * @param hiddenFilter
     */
    public void withHiddenFilter(boolean hiddenFilter) {
        this.hiddenFilter = hiddenFilter;
    }

    /**
     * @return
     */
    public NorthboundRequest build() {
        NorthboundRequest request = null;
        if (this.method == null) {
            return request;
        }
        switch (this.method) {
            case "ALL":
                FilteringCollection collection = null;
                if (this.filterDefinitions != null) {
                    collection = new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions);
                }
                request = new AllRequest(mediator, getRequestIdentifier(), collection);
                break;
            case "ACT":
                if (this.resource != null) {
                    Object[] arguments = null;
                    if (this.argument != null) {
                        if (this.argument.getClass().isArray()) {
                            arguments = (Object[]) this.argument;

                        } else {
                            arguments = new Object[]{this.argument};
                        }
                    }
                    request = new ResourceActRequest(mediator, getRequestIdentifier(), serviceProvider, service, resource, arguments);
                }
                break;
            case "DESCRIBE":
                if (this.resource != null) {
                    request = new ResourceRequest(mediator, getRequestIdentifier(), serviceProvider, service, resource);

                } else if (service != null) {
                    if (this.listElements) {
                        request = new ResourcesRequest(mediator, getRequestIdentifier(), serviceProvider, service, this.filterDefinitions == null ? null : new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions));

                    } else {
                        request = new ServiceRequest(mediator, getRequestIdentifier(), serviceProvider, service, null);
                    }
                } else if (serviceProvider != null) {
                    if (this.listElements) {
                        request = new ServicesRequest(mediator, getRequestIdentifier(), serviceProvider, this.filterDefinitions == null ? null : new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions));

                    } else {
                        request = new ServiceProviderRequest(mediator, getRequestIdentifier(), serviceProvider, null);
                    }
                } else {
                    request = new ServiceProvidersRequest(mediator, getRequestIdentifier(), this.filterDefinitions == null ? null : new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions));
                }
                break;
            case "GET":
                request = new AttributeGetRequest(mediator, getRequestIdentifier(), serviceProvider, service, resource, attribute);
                break;
            case "SET":
                request = new AttributeSetRequest(mediator, getRequestIdentifier(), serviceProvider, service, resource, attribute, argument);
                break;
            case "SUBSCRIBE":
                Object[] arguments = this.argument != null ? (this.argument.getClass().isArray() ? (Object[]) this.argument : new Object[]{this.argument}) : null;

                if (arguments == null || arguments.length == 0 || !NorthboundRecipient.class.isAssignableFrom(arguments[0].getClass())) {
                    break;
                }
                if (this.resource != null) {
                    request = new AttributeSubscribeRequest(mediator, getRequestIdentifier(), serviceProvider, service, resource, attribute, (NorthboundRecipient) arguments[0],
                    	(arguments.length > 1 ? ((JSONArray) arguments[1]) : new JSONArray()),(arguments.length > 2 ? ((String) arguments[2]) : String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY)));
                } else {
                    request = new RegisterAgentRequest(mediator, getRequestIdentifier(), serviceProvider, service, (NorthboundRecipient) arguments[0], 
                    	(SnaFilter) (arguments.length > 1 ? arguments[1] : null),(arguments.length > 2 ? ((String) arguments[2]) : String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY)));
                }
                break;
            case "UNSUBSCRIBE":
                String arg = CastUtils.cast(mediator.getClassLoader(), String.class, this.argument);
                if (this.resource != null) {
                    request = new AttributeUnsubscribeRequest(mediator, getRequestIdentifier(), serviceProvider, service, resource, attribute, arg);
                } else {
                    request = new UnregisterAgentRequest(mediator, getRequestIdentifier(), arg);
                }
                break;
            default:
                break;
        }
        return request;
    }
}
