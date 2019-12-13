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
package org.eclipse.sensinact.gateway.agent.storage.internal;

import org.eclipse.sensinact.gateway.api.core.DataResource;
import org.eclipse.sensinact.gateway.api.core.LocationResource;
import org.eclipse.sensinact.gateway.api.core.Resource;
import org.eclipse.sensinact.gateway.api.message.AbstractMessageAgentCallback;
import org.eclipse.sensinact.gateway.api.message.ErrorMessageImpl;
import org.eclipse.sensinact.gateway.api.message.LifecycleMessageImpl;
import org.eclipse.sensinact.gateway.api.message.ResponseMessage;
import org.eclipse.sensinact.gateway.api.message.UpdateMessageImpl;
import org.eclipse.sensinact.gateway.api.message.LifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.DefaultErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class StorageAgent extends AbstractMessageAgentCallback {
    private static final Logger LOG = LoggerFactory.getLogger(StorageAgent.class);
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final StorageConnection storageConnection;

    /**
     * Constructor
     *
     * @param mediator the associated {@link Mediator}
     * @throws IOException Exception on connection problem
     */
    public StorageAgent(String login, String password, String broker, Mediator mediator) throws IOException {
        super();
        this.storageConnection = new StorageConnection(mediator, broker, login, password);
        super.setErrorHandler(new DefaultErrorHandler(ErrorHandler.Policy.LOG|ErrorHandler.Policy.IGNORE));
    }

    /**
     * @inheritDoc
     * @see AbstractMessageAgentCallback#doHandle(UpdateMessageImpl)
     */
    @Override
    public void doHandle(UpdateMessageImpl message) {
        String path = message.getPath();
        LOG.debug("storage agent informed of an update on {}...", path);
        String[] elements = UriUtils.getUriElements(path);
        String serviceProvider = elements[0];
        String service = elements[1];
        String resource = elements[2];
        JSONObject initial = message.getNotification();
        this.doHandle(serviceProvider, service, resource, initial);
    }

    /**
     * @inheritDoc
     * @see AbstractMessageAgentCallback#doHandle(LifecycleMessageImpl)
     */
    @Override
    public void doHandle(LifecycleMessageImpl message) {
        String path = message.getPath();
        if (!Lifecycle.RESOURCE_APPEARING.equals(message.getType()) || Resource.Type.ACTION.equals(message.getNotification(Resource.Type.class, "type"))) {
            return;
        }
        String[] elements = UriUtils.getUriElements(path);
        String serviceProvider = elements[0];
        String service = elements[1];
        String resource = elements[2];
        JSONObject initial = new JSONObject(message.getJSON()).getJSONObject("initial");
        this.doHandle(serviceProvider, service, resource, initial);
    }

    /**
     * @param serviceProvider the service provider
     * @param service         the service
     * @param resource        the resource
     * @param content         the content
     */
    private void doHandle(String serviceProvider, String service, String resource, JSONObject content) {
        Object initialValue = content.opt(DataResource.VALUE);
        if (JSONObject.NULL.equals(initialValue)) {
            //exclude initial null value
            LOG.debug("Unexpected null initial value error {}/{}/{}/{}...", serviceProvider, service, resource, initialValue);
            return;
        }
        Long timestamp = (Long) content.opt("timestamp");
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        String timestampStr = FORMAT.format(new Date(timestamp));
        if (LocationResource.LOCATION.equalsIgnoreCase(resource)) {
            //set location and escape
            try {
                super.setLocation(serviceProvider, String.valueOf(initialValue));
            } catch (Exception e) {
                LOG.debug("Unexpected location error {}", e.getMessage(), e);
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(DataResource.VALUE, initialValue);
        jsonObject.put("device", serviceProvider);
        jsonObject.put("service", service);
        jsonObject.put("resource", resource);
        jsonObject.put(DataResource.VALUE, initialValue);
        jsonObject.put("timestamp", timestampStr);
        LOG.debug("pushing to stack {}/{}/{}/{}...", serviceProvider, service, resource, initialValue);
        this.storageConnection.stack.push(jsonObject);
        LOG.debug("...done");
    }

    /**
     * @inheritDoc
     * @see AbstractMessageAgentCallback#doHandle(ErrorMessageImpl)
     */
    public void doHandle(ErrorMessageImpl message) {
    }

    /**
     * @inheritDoc
     * @see AbstractMessageAgentCallback#doHandle(ResponseMessage)
     */
    public synchronized void doHandle(ResponseMessage message) {
    }

    /**
     * @inheritDoc
     * @see AbstractMessageAgentCallback#stop()
     */
    public void stop() {
        this.storageConnection.close();
    }
}
