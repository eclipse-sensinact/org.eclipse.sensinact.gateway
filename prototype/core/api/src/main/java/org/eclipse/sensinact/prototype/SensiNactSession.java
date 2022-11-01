/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation 
**********************************************************************/
package org.eclipse.sensinact.prototype;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.prototype.notification.ClientActionListener;
import org.eclipse.sensinact.prototype.notification.ClientDataListener;
import org.eclipse.sensinact.prototype.notification.ClientLifecycleListener;
import org.eclipse.sensinact.prototype.notification.ClientMetadataListener;

public interface SensiNactSession {

    /**
     * The id of this session
     * 
     * @return
     */
    String getSessionId();

    /**
     * Get the time at which this session will expire
     * 
     * @return
     */
    Instant getExpiry();

    /**
     * Extend this session to expire after the given time period
     */
    void extend(Duration duration);

    /**
     * true if this session is expired
     * 
     * @return
     */
    boolean isExpired();

    /**
     * Expire this session immediately
     */
    void expire();

    /**
     * Get the active listener registrations
     * 
     * @return a Map of subscription identifier to list of listened topics
     */
    Map<String, List<String>> activeListeners();

    /**
     * 
     * @param topics - topic strings, omitting the initial segment (e.g. LIFECYCLE)
     * @param cdl    a listener, or null if data events are ignored
     * @param cml    a listener, or null if metadata events are ignored
     * @param cll    a listener, or null if lifecycle events are ignored
     * @param cal    a listener, or null if action events are ignored
     * @return a new registration identifier
     */
    String addListener(List<String> topics, ClientDataListener cdl, ClientMetadataListener cml,
            ClientLifecycleListener cll, ClientActionListener cal);

    /**
     * Remove a registered listener
     * 
     * @param id the registration identifier
     */
    void removeListener(String id);

    /**
     * Get the value of a resource
     * 
     * @param <T>
     * @param provider
     * @param service
     * @param resource
     * @param clazz
     * @return
     * @throws ClassCastException       if the value cannot be cast to the relevant
     *                                  type
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    <T> T getResourceValue(String provider, String service, String resource, Class<T> clazz);

    /**
     * Set the value of a resource with the current time
     * 
     * @param provider
     * @param service
     * @param resource
     * @param o
     * @return
     * @throws ClassCastException       if the value cannot be cast to the relevant
     *                                  type for the resource
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    void setResourceValue(String provider, String service, String resource, Object o);

    /**
     * Set the value of a resource with the supplied time
     * 
     * @param provider
     * @param service
     * @param resource
     * @param o
     * @return
     * @throws ClassCastException       if the value cannot be cast to the relevant
     *                                  type for the resource
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     * @throws IllegalStateException    if the supplied time is before the current
     *                                  time for the resource
     */
    void setResourceValue(String provider, String service, String resource, Object o, Instant instant);

    /**
     * Get the metadata for a resource
     * 
     * @param provider
     * @param service
     * @param resource
     * @return
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    Map<String, Object> getResourceMetadata(String provider, String service, String resource);

    /**
     * Set the metadata for a resource
     * 
     * @param provider
     * @param service
     * @param resource
     * @param metadata
     * @return
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    void setResourceMetadata(String provider, String service, String resource, Map<String, Object> metadata);

    /**
     * Get a metadata value for a resource
     * 
     * @param <T>
     * @param provider
     * @param service
     * @param resource
     * @param metadata
     * @return
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    Map<String, Object> getResourceMetadataValue(String provider, String service, String resource, String metadata);

    /**
     * Set a metadata value for a resource
     * 
     * @param <T>
     * @param provider
     * @param service
     * @param resource
     * @param metadata
     * @param value
     * @return
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    void setResourceMetadata(String provider, String service, String resource, String metadata, Object value);

    /**
     * Perform an action on a resource
     * 
     * @param provider
     * @param service
     * @param resource
     * @param parameters
     * @return
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    Object actOnResource(String provider, String service, String resource, Object[] parameters);

    /**
     * Get the description of a resource
     * 
     * @param provider
     * @param service
     * @param resource
     * @return
     * @throws IllegalArgumentException if there is no resource at the given
     *                                  location
     */
    ResourceDescription describeResource(String provider, String service, String resource);

    /**
     * Get the description of a resource
     * 
     * @param provider
     * @param service
     * @return
     * @throws IllegalArgumentException if there is no service at the given location
     */
    ServiceDescription describeService(String provider, String service);

    /**
     * Get the description of a resource
     * 
     * @param provider
     * @return
     * @throws IllegalArgumentException if there is no provider at the given
     *                                  location
     */
    ProviderDescription describeProvider(String provider);

    /**
     * Get the list of providers
     * 
     * @return
     */
    List<ProviderDescription> listProviders();
}
