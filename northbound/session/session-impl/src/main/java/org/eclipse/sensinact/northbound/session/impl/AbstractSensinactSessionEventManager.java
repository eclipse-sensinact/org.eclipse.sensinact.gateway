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
package org.eclipse.sensinact.northbound.session.impl;

import static org.osgi.service.typedevent.TypedEventConstants.TYPED_EVENT_TOPICS;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.typedevent.TypedEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSensinactSessionEventManager
        implements TypedEventHandler<ResourceNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSensinactSessionEventManager.class);

    protected final String sessionId;
    protected final String subscriptionId;
    protected final Authorizer authorizer;

    private final List<String> requestedTopics;
    private final AtomicInteger state = new AtomicInteger(0);
    private final AtomicReference<ServiceRegistration<?>> registration = new AtomicReference<ServiceRegistration<?>>();


    public AbstractSensinactSessionEventManager(String sessionId,
            String subscriptionId, List<String> topics, Authorizer authorizer) {
        this.sessionId = sessionId;
        this.subscriptionId = subscriptionId;
        this.authorizer = authorizer;
        requestedTopics = List.copyOf(topics);
    }

    /**
     * To be called in the sub-class constructor once registered topics are available
     * @param context
     */
    public void register(BundleContext context) {
        if(!state.compareAndSet(0, 1)) {
            throw new IllegalStateException("This listener has already been registered");
        }
        List<String> registeredTopics = getRegisteredTopics();
        if(registeredTopics.isEmpty()) {
            throw new IllegalArgumentException("No topics are registered");
        }
        registration.set(
                context.registerService(TypedEventHandler.class, this,
                        new Hashtable<>(Map.of(TYPED_EVENT_TOPICS, registeredTopics))));
        if(state.get() != 1) {
            destroy();
        }
    }

    public List<String> getRequestedTopics() {
        return requestedTopics;
    }

    public abstract List<String> getRegisteredTopics();

    public void destroy() {
        state.set(2);
        try {
            ServiceRegistration<?> reg = registration.getAndSet(null);
            if(reg != null) {
                reg.unregister();
            }
        } catch (Exception e) {
            LOG.warn("Unexpected error tidying up session {}", subscriptionId);
        }
    }

    @Override
    public void notify(String topic, ResourceNotification event) {
        if(event instanceof ResourceDataNotification rdn) {
            notifyData(topic, rdn);
        } else if (event instanceof ResourceMetaDataNotification rmn) {
            notifyMetdata(topic, rmn);
        } else if (event instanceof LifecycleNotification ln) {
            notifyLifecycle(topic, ln);
        } else if (event instanceof ResourceActionNotification ran) {
            notifyAction(topic, ran);
        } else {
            LOG.error("Unknown event with data {}", event);
        }
    }

    protected abstract void notifyLifecycle(String topic, LifecycleNotification ln);

    protected abstract void notifyData(String topic, ResourceDataNotification notification);

    protected abstract void notifyMetdata(String topic, ResourceMetaDataNotification notification);

    protected abstract void notifyAction(String topic, ResourceActionNotification notification);
}
