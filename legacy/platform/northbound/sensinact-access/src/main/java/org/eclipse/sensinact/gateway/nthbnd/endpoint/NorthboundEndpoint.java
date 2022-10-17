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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.sensinact.gateway.core.AnonymousSession;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.filtering.FilteringCollection;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.UnsubscribeResponse;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * A NorthboundEndpoint is a connection point to a sensiNact instance
 * for an northbound access service
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class NorthboundEndpoint {
	
	private static final Logger LOG = LoggerFactory.getLogger(NorthboundEndpoint.class);
	
    private Session session;
    private NorthboundMediator mediator;

    /**
     * Constructor
     *
     * @param mediator       the {@link NorthboundMediator} that will allow
     *                       the NothboundEndpoint to be instantiated to interact with the
     *                       OSGi host environment
     * @param authentication the {@link Authentication}  that will allow
     *                       the NothboundEndpoint to be instantiated to build the appropriate
     *                       {@link Session}
     * @throws InvalidCredentialException
     */
    public NorthboundEndpoint(NorthboundMediator mediator, Authentication<?> authentication) throws InvalidCredentialException {
        this.mediator = mediator;
        this.session = this.mediator.getSession(authentication);
        if (this.session == null) 
            throw new NullPointerException("null sensiNact session");
    }

    /**
     * Returns the String identifier of the {@link Session} of this
     * NorthboundEndpoint
     *
     * @return the String identifier of this NorthboundEndpoint's
     * {@link Session}
     */
    public String getSessionToken() {
        return this.session.getSessionId();
    }

    /**
     * Executes the {@link NorthboundRequest} passed as parameter
     * and returns the execution result in the <code>&lt;F&gt;</code>
     * typed format
     *
     * @param request        the {@link NorthboundRequest} to be executed
     * @param responseFormat the {@link ResponseFormat} allowing to
     *                       format the execution result Object inn the expected format
     * @return the execution result Object of this request in
     * the appropriate format
     */
    public AccessMethodResponse<?> execute(NorthboundRequest request) {
        AccessMethodResponse<?> result = null;
        Argument[] arguments = request.getExecutionArguments();
        final String meth =  request.getMethod();
        final Class<?>[] parameterTypes = Argument.getParameterTypes(arguments);
    	Predicate<Method> predicate = m -> {
			if(!m.getName().equals(meth))
				return false;
			Class<?>[] types = m.getParameterTypes();
			if(types.length < parameterTypes.length && !m.isVarArgs())
				return false;
			if(types.length == (parameterTypes.length +1) && !m.isVarArgs())
				return false;
			if(types.length > (parameterTypes.length +1))
				return false;
			for(int i=0;i<types.length;i++) {
				if(i == (types.length-1) && (types.length == (parameterTypes.length +1)||types.length < parameterTypes.length))
					continue;
				if(!types[i].isAssignableFrom(parameterTypes[i]))
					return false;
			}
			return true;
	    };
        try {
        	Method[] methods = getClass().getDeclaredMethods();
        	Optional<Method> opt = Arrays.stream(methods).filter(predicate).findFirst();        	
            Method method = opt.isPresent()?opt.get():null;
            
            if(method != null) {       
            	Object[] args = Argument.getParameters(arguments);                
	            if(method.isVarArgs()) {
	            	if(method.getParameterTypes().length == parameterTypes.length+1) {	            
		            	Object[] _args = new Object[parameterTypes.length+1];
		            	System.arraycopy(args, 0, _args, 0, parameterTypes.length);
		            	_args[parameterTypes.length] = (Object[])null;
		            	args = _args;
	            	} else if(method.getParameterTypes().length < parameterTypes.length) {
	            		Object[] _args = new Object[method.getParameterTypes().length];
	            		Object[] _var = new Object[(parameterTypes.length - method.getParameterTypes().length) + 1];
	            		if(method.getParameterTypes().length>1)
	            			System.arraycopy(args, 0, _args, 0, method.getParameterTypes().length-1);
	            		int n=0;
	            		for(int i=method.getParameterTypes().length - 1; i < parameterTypes.length; i++)
	            			_var[n++]=args[i];
	            		_args[method.getParameterTypes().length-1] = _var;
	            		args = _args;
	            	}	            	
	            }
	            result = (AccessMethodResponse<?>) method.invoke(this, args);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * @param login
     * @param password
     * @param account
     * @param accountType
     * @throws SecuredAccessException
     */
    public void registerUser(String login, String password, String account, String accountType) throws SecuredAccessException {
    	if(!(session instanceof AnonymousSession)) 
    		throw new SecuredAccessException("Invalid Session");
		((AnonymousSession)session).registerUser(login, password, account, accountType);
    }

    /**
     * @param account
     * @throws SecuredAccessException
     */
    public void renewUserPassword(String account) throws SecuredAccessException {    	
    	if(!(session instanceof AnonymousSession))
    		throw new SecuredAccessException("Invalid Session");
		((AnonymousSession)session).renewPassword(account);
    }
    
    /**
     * Registers an {@link SnaAgent} whose lifetime will be linked
     * to the {@link Session} of this NorthboundEndpoint
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param callback
     * @param filter
     * @return the {@link SnaAgent} registration response
     */
    public SubscribeResponse registerAgent(String requestIdentifier, AbstractMidAgentCallback callback, SnaFilter filter) {
        return session.registerSessionAgent(requestIdentifier, callback, filter);
    }

    /**
     * Unregisters the {@link SnaAgent} whose String identifier is
     * passed as parameter
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param agentId
     * @return the {@link SnaAgent} unregistration response
     */
    public UnsubscribeResponse unregisterAgent(String requestIdentifier, String agentId) {
        return session.unregisterSessionAgent(requestIdentifier, agentId);
    }

    /**
     * Gets the all JSONObject formated list of service
     * providers, services and resources, including their
     * location
     *
     * @param requestIdentifier the String identifier of
     *                          the request calling this method
     * @return the JSON formated list of all the
     * model instances' hierarchies and wrapped into a {@link
     * DescribeResponse}
     */
    public DescribeResponse<String> all(String requestIdentifier) {
        return this.all(requestIdentifier, null, null);
    }

    /**
     * Gets the all JSONObject formated list of service
     * providers, services and resources, as well as
     * their location, and compliant with the String
     * filter passed as parameter
     *
     * @param requestIdentifier the String identifier of
     *                          the request calling this method
     * @param filter            the LDAP formated String filter allowing
     *                          to discriminate the targeted service providers, services,
     *                          and/or resources
     * @return the JSON formated list of all the model instances'
     * hierarchies according to the specified LDAP filter and
     * wrapped into {@link DescribeResponse}
     */
    public DescribeResponse<String> all(String requestIdentifier, String filter) {
        return this.all(requestIdentifier, filter, null);
    }

    /**
     * Gets the all JSONObject formated list of service
     * providers, services and resources, as well as
     * their location, and compliant with the String
     * filter passed as parameter
     *
     * @param requestIdentifier the String identifier of the
     *                          request calling this method
     * @param filter            the LDAP formated String filter allowing
     *                          to discriminate the targeted service providers, services,
     *                          and/or resources
     * @param filterCollection  the {@link FilteringCollection}
     *                          specifying the set of filters to be applied on the result
     * @return the JSON formated list of all the model
     * instances' hierarchies filtered according to the specified
     * filters collection and wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<String> all(String requestIdentifier, String filter, FilteringCollection filterCollection) {
        return session.getAll(requestIdentifier, filter, filterCollection);
    }

    /**
     * Get the list of service providers and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @return the JSON formated list of the registered
     * service providers wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<String> serviceProvidersList(String requestIdentifier) {
        return this.serviceProvidersList(requestIdentifier, null);
    }

    /**
     * Get the list of service providers and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param filterCollection  the {@link FilteringCollection} specifying
     *                          the set of filters to be applied on the result
     * @return the JSON formated list of the service
     * providers list filtered according to the specified
     * filters collection and wrapped into {@link DescribeResponse}
     */
    public DescribeResponse<String> serviceProvidersList(String requestIdentifier, FilteringCollection filterCollection) {
        return session.getProviders(requestIdentifier, filterCollection);
    }

    /**
     * Get the information of a specific service providers and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @return the JSON formated description of the specified
     * service provider wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<JsonObject> serviceProviderDescription(String requestIdentifier, String serviceProviderId) {
        return session.getProvider(requestIdentifier, serviceProviderId);
    }

    /**
     * Get the list of services of a service provider and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @return the JSON formated list of the services belonging to
     * the specified service provider wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<String> servicesList(String requestIdentifier, String serviceProviderId) {
        return this.servicesList(requestIdentifier, serviceProviderId, null);
    }

    /**
     * Get the list of services of a service provider and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param filterCollection  the {@link FilteringCollection} specifying
     *                          the set of filters to be applied on the result
     * @return the JSON formated list of the services belonging to
     * the specified service provider, filtered using the filters
     * collection and wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<String> servicesList(String requestIdentifier, String serviceProviderId, FilteringCollection filterCollection) {
        return session.getServices(requestIdentifier, serviceProviderId, filterCollection);
    }

    /**
     * Get the information of a specific service and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @return the JSON formated description of the specified
     * service wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<JsonObject> serviceDescription(String requestIdentifier, String serviceProviderId, String serviceId) {
        return session.getService(requestIdentifier, serviceProviderId, serviceId);
    }

    /**
     * Get the list of resources of a service and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @return the JSON formated list of the resources belonging
     * to the specified service, wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<String> resourcesList(String requestIdentifier, String serviceProviderId, String serviceId) {
        return this.resourcesList(requestIdentifier, serviceProviderId, serviceId, null);
    }

    /**
     * Get the list of resources of a service and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param filterCollection  the {@link FilteringCollection}
     *                          specifying the set of filters to be applied on the result
     * @return the JSON formated list of the resources
     * belonging to the specified service, filtered using the
     * filters collection and wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<String> resourcesList(String requestIdentifier, String serviceProviderId, String serviceId, FilteringCollection filterCollection) {
        return session.getResources(requestIdentifier, serviceProviderId, serviceId, filterCollection);
    }

    /**
     * Get the information of a specific resource and returns it
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param resourceId        the String identifier of the resource
     * @return the JSON formated description of the specified
     * resource wrapped into a {@link DescribeResponse}
     */
    public DescribeResponse<JsonObject> resourceDescription(String requestIdentifier, String serviceProviderId, String serviceId, String resourceId) {
        return session.getResource(requestIdentifier, serviceProviderId, serviceId, resourceId);
    }

    /**
     * Perform a sNa GET on a resource
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param resourceId        the String identifier of the resource
     * @param attributeId       the String identifier of the attribute
     * @return
     */
    public GetResponse get(String requestIdentifier, String serviceProviderId, String serviceId, String resourceId, 
    		String attributeId, Object...args) {
        return session.get(requestIdentifier, serviceProviderId, serviceId, resourceId, attributeId, args);
    }

    /**
     * Perform a sNa SET on a resource
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param resourceId        the String identifier of the resource
     * @param attributeId       the String identifier of the attribute
     * @param value
     * @return
     */
    public SetResponse set(String requestIdentifier, String serviceProviderId, String serviceId, String resourceId, 
    	String attributeId, Object value, Object...args) {
        return session.set(requestIdentifier, serviceProviderId, serviceId, resourceId, attributeId, value, args);
    }

    /**
     * Perform a sNa ACT on a resource
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param resourceId        the String identifier of the resource
     * @param arguments
     * @return
     */
    public ActResponse act(String requestIdentifier, String serviceProviderId, String serviceId, String resourceId, Object[] arguments) {
        return session.act(requestIdentifier, serviceProviderId, serviceId, resourceId, arguments);
    }

    /**
     * Perform a subscription to a resource
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param resourceId        the String identifier of the resource
     * @param attributeId       the String identifier of the attribute
     * @param recipient
     * @param conditions
     * @return
     */
    public SubscribeResponse subscribe(String requestIdentifier, String serviceProviderId, String serviceId, String resourceId, String attributeId, 
    		NorthboundRecipient recipient, JsonArray conditions, String policy, Object...args) {
        return session.subscribe(requestIdentifier, serviceProviderId, serviceId, resourceId, recipient, conditions, policy, args);
    }

    /**
     * Perform an unsubscription to a resource
     *
     * @param requestIdentifier the String identifier of the request
     *                          calling this method
     * @param serviceProviderId the String identifier of the service provider
     * @param serviceId         the String identifier of the service
     * @param resourceId        the String identifier of the resource
     * @param attributeId       the String identifier of the attribute
     * @param subscriptionId
     * @return
     */
    public UnsubscribeResponse unsubscribe(String requestIdentifier, String serviceProviderId, String serviceId, String resourceId, String attributeId,
    		String subscriptionId, Object...args) {
        return session.unsubscribe(requestIdentifier, serviceProviderId, serviceId, resourceId, subscriptionId, args);
    }
}