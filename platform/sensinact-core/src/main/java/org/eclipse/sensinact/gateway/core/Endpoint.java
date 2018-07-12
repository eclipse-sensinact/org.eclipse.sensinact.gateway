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
package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An Endpoint allows to invoke access methods on a sensiNact instance. It allows
 * also to retrieve the descriptions of available service providers, services, and
 * resources
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Endpoint {
    /**
     * Returns the JSON formated list of all registered, and accessible
     * by the user whose String public key is passed as parameter, resource
     * model instances from the local sensiNact instance, as well as from the
     * connected remote ones
     *
     * @param publicKey the String public key of the user for which to
     *                  retrieve the list of accessible resource model instances
     * @return the JSON formated list of the resource model instances for
     * the specified user.
     */
    String getAll(String publicKey);

    /**
     * Returns the JSON formated list of all registered, accessible
     * to the user whose String public key is passed as parameter, and
     * compliant to the specified String LDAP formated filter, resource
     * model instances from the local sensiNact instance, as well as from
     * the connected remote ones
     *
     * @param publicKey the String public key of the user for which to
     *                  retrieve the list of accessible resource model instances
     * @param filter    the String LDAP formated filter
     * @return the JSON formated list of the resource model instances for
     * the specified user and compliant to the specified filter.
     */
    String getAll(String publicKey, String filter);

    /**
     * Returns the JSON formated list of available service providers for
     * the user whose public key is passed as parameter
     *
     * @param publicKey the String public key of the user requiring the
     *                  list of available service providers
     * @return the JSON formated list of available service providers
     */
    String getProviders(String publicKey);

    /**
     * Returns the JSON formated description of the service provider whose
     * String identifier is passed as parameter
     *
     * @param publicKey         the String public key of the user requiring the
     *                          description
     * @param serviceProviderId the String identifier of the
     *                          service provider to return the description of
     * @return the JSON formated description of the specified service provider
     */
    String getProvider(String publicKey, String serviceProviderId);

    /**
     * Returns the JSON formated list of available services for the service
     * provider whose String identifier is passed as parameter
     *
     * @param publicKey         the String public key of the user requiring the
     *                          list of available services
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the services
     * @return the JSON formated list of available services for the
     * specified service provider
     */
    String getServices(String publicKey, String serviceProviderId);

    /**
     * Returns the JSON formated description of the service whose String
     * identifier is passed as parameter, and held by the specified service
     * provider
     *
     * @param publicKey         the String public key of the user requiring the
     *                          description
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service
     * @param serviceId         the String identifier of the service to return the
     *                          description of
     * @return the JSON formated description of the specified service
     */
    String getService(String publicKey, String serviceProviderId, String serviceId);

    /**
     * Returns the JSON formated list of available resources, for the service
     * and service provider whose String identifiers are passed as parameter
     *
     * @param publicKey         the String public key of the user requiring the
     *                          list of available resources
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service
     * @param serviceId         the String identifier of the service providing
     *                          the resources
     * @return the JSON formated list of available resources for the
     * specified service and service provider
     */
    String getResources(String publicKey, String serviceProviderId, String serviceId);

    /**
     * Returns the JSON formated description of the resource whose String
     * identifier is passed as parameter, and held by the service
     * provider and service whose String identifiers are also passed as
     * parameter
     *
     * @param publicKey         the String public key of the user requiring the
     *                          description
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service, providing the resource
     * @param serviceId         the String identifier of the service providing
     *                          the resource
     * @param resourceId        the String identifier  of the resource
     *                          to return the description of
     * @return the JSON formated description of the specified resource
     */
    String getResource(String publicKey, String serviceProviderId, String serviceId, String resourceId);

    /**
     * Invokes the GET access method on the resource whose String identifier
     * is passed as parameter, held by the specified service provider and
     * service, and for the user whose String public key is also passed as
     * parameter
     *
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service providing the resource
     *                          on which applies the access method call
     * @param serviceId         the String identifier of the service providing
     *                          the resource on which applies the access method call
     * @param resourceId        the String identifier  of the resource
     *                          on which applies the access method call
     * @param attributeId       the String identifier of the resource's attribute
     *                          targeted by the access method call
     * @return the JSON formated response of the GET access method invocation
     */
    JSONObject get(String publicKey, String serviceProviderId, String serviceId, String resourceId, String attributeId);

    /**
     * Invokes the SET access method on the resource whose String identifier
     * is passed as parameter, held by the specified service provider and
     * service
     *
     * @param publicKey         the String public key of the user invoking
     *                          the access method
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service providing the resource
     *                          on which applies the access method call
     * @param serviceId         the String identifier of the service providing
     *                          the resource on which applies the access method call
     * @param resourceId        the String identifier  of the resource
     *                          on which applies the access method call
     * @param attributeId       the String identifier of the resource's attribute
     *                          targeted by the access method call
     * @param parameter         the value object to be set
     * @return the JSON formated response of the SET access method
     * invocation
     */
    JSONObject set(String publicKey, String serviceProviderId, String serviceId, String resourceId, String attributeId, Object parameter);

    /**
     * Invokes the ACT access method on the resource whose String identifier
     * is passed as parameter, held by the specified service provider and
     * service
     *
     * @param publicKey         the String public key of the user invoking
     *                          the access method
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service providing the resource
     *                          on which applies the access method call
     * @param serviceId         the String identifier of the service providing
     *                          the resource on which applies the access method call
     * @param resourceId        the String identifier  of the resource
     *                          on which applies the access method call
     * @param parameters        the Objects array parameterizing the
     *                          call
     * @return the JSON formated response of the ACT access method
     * invocation
     */
    JSONObject act(String publicKey, String serviceProviderId, String serviceId, String resourceId, Object[] parameters);

    /**
     * Invokes the SUBSCRIBE access method on the resource whose String
     * identifier is passed as parameter, held by the specified service
     * provider and service
     *
     * @param publicKey         the String public key of the user invoking
     *                          the access method
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service providing the resource
     *                          on which applies the access method call
     * @param serviceId         the String identifier of the service providing
     *                          the resource on which applies the access method call
     * @param resourceId        the String identifier  of the resource
     *                          on which applies the access method call
     * @param recipient         the {@link Recipient} to which the update events
     *                          generated by the subscription will be transmitted
     * @param conditions        the JSON formated set of constraints applying
     *                          on the subscription to be created
     * @return the JSON formated response of the SUBSCRIBE access method
     * invocation
     */
    JSONObject subscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId, Recipient recipient, JSONArray conditions);

    /**
     * Invokes the UNSUBSCRIBE access method on the resource whose String
     * identifier is passed as parameter, held by the specified service
     * provider and service
     *
     * @param publicKey         the String public key of the user invoking
     *                          the access method
     * @param serviceProviderId the String identifier of the
     *                          service provider holding the service providing the resource
     *                          on which applies the access method call
     * @param serviceId         the String identifier of the service providing
     *                          the resource on which applies the access method call
     * @param resourceId        the String identifier  of the resource
     *                          on which applies the access method call
     * @param subscriptionId    the String identifier of the subscription
     *                          to be deleted
     * @return the JSON formated response of the UNSUBSCRIBE access method
     * invocation
     */
    JSONObject unsubscribe(String publicKey, String serviceProviderId, String serviceId, String resourceId, String subscriptionId);
}
