/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.core.filtering.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.util.CastUtils;

import jakarta.json.JsonArray;

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
    private List<Argument> arguments;
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
        this.arguments = new ArrayList<>();
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
     * Define an argument that will be used to parameterize the associated method call
     *
     * @param argument {@link Argument} wrapping the parameter 
     * 
     * @return this NorthboundRequestBuilder
     */
    public NorthboundRequestBuilder withArgument(Argument argument) {
        this.arguments.add(argument);
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
        if (this.method == null) 
            return request;
        switch (this.method) {
            case "ALL":
                FilteringCollection collection = null;
                if (this.filterDefinitions != null) 
                    collection = new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions);
                request = new AllRequest(getRequestIdentifier(), collection);
                break;
            case "ACT":
                if (this.resource != null) 
                    request = new ResourceActRequest(getRequestIdentifier(), serviceProvider, service, resource, 
                       this.arguments.stream().<List<Object>>collect(ArrayList::new,(l,a)->{l.add(a.value);},List::addAll).toArray());
                break;
            case "DESCRIBE":
                if (this.resource != null) 
                    request = new ResourceRequest(getRequestIdentifier(), serviceProvider, service, resource);
                else if (service != null) {
                    if (this.listElements) 
                        request = new ResourcesRequest(getRequestIdentifier(), serviceProvider, service, 
                        	this.filterDefinitions == null ? null : new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions));
                    else 
                        request = new ServiceRequest(getRequestIdentifier(), serviceProvider, service, null);                    
                } else if (serviceProvider != null) {
                    if (this.listElements) 
                        request = new ServicesRequest(getRequestIdentifier(), serviceProvider, 
                        	this.filterDefinitions == null ? null : new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions));
                    else 
                        request = new ServiceProviderRequest(getRequestIdentifier(), serviceProvider, null);
                    
                } else 
                    request = new ServiceProvidersRequest(getRequestIdentifier(), 
                    	this.filterDefinitions == null ? null : new FilteringCollection(mediator, this.hiddenFilter, this.filterDefinitions));                
                break;
            case "GET":
                request = new AttributeGetRequest(getRequestIdentifier(), serviceProvider, service, resource, 
                	attribute, this.arguments==null || this.arguments.size()==0 ?null:this.arguments.toArray(new Argument[0]));
                break;
            case "SET":
            	List<Argument> extraArguments = this.arguments.size()>1?this.arguments.subList(1, this.arguments.size()-1):null;
                request = new AttributeSetRequest(getRequestIdentifier(), serviceProvider, service, resource, 
                	attribute, this.arguments.get(0).value, extraArguments==null|| extraArguments.size()==0?null:extraArguments.toArray(new Argument[0]));
                break;
            case "SUBSCRIBE":
                if (this.arguments == null || this.arguments.size() == 0) 
                    break;
                NorthboundRecipient northboundRecipient = null;
                try{
                	northboundRecipient = (NorthboundRecipient)this.arguments.get(0).value;
                } catch(ClassCastException e){
                	break;
                }
                String policy =  this.arguments.size() > 2?((String) this.arguments.get(2).value):String.valueOf(ErrorHandler.Policy.DEFAULT_POLICY);
            	extraArguments = this.arguments.size() > 3?this.arguments.subList(3, this.arguments.size()-1):null;
                if (this.resource != null) 
                    request = new AttributeSubscribeRequest(getRequestIdentifier(), serviceProvider, service, resource, attribute, 
                    	northboundRecipient, (JsonArray)(this.arguments.size() > 1?this.arguments.get(1).value:JsonArray.EMPTY_JSON_ARRAY), 
                    	policy, extraArguments==null|| extraArguments.size()==0?null:extraArguments.toArray(new Argument[0]));
                else 
                    request = new RegisterAgentRequest(getRequestIdentifier(), serviceProvider, service, northboundRecipient, 
                    	(SnaFilter) (this.arguments.size() > 1 ?this.arguments.get(1).value:null), policy);               
                break;
            case "UNSUBSCRIBE":
            	extraArguments = this.arguments.size()>1?this.arguments.subList(1, this.arguments.size()-1):null;
            	String subcriptionId = CastUtils.cast(String.class, this.arguments.get(0).value);
                if (this.resource != null)
                    request = new AttributeUnsubscribeRequest(getRequestIdentifier(), serviceProvider, service, resource, 
                    	attribute, subcriptionId, extraArguments==null|| extraArguments.size()==0?null:extraArguments.toArray(new Argument[0]));
                else 
                    request = new UnregisterAgentRequest(getRequestIdentifier(), subcriptionId);                
                break;
            default:
                break;
        }
        return request;
    }
}
