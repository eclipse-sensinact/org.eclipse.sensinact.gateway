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
package org.eclipse.sensinact.gateway.agent.storage.generic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.sensinact.gateway.common.execution.DefaultErrorHandler;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class StorageAgent extends AbstractMidAgentCallback {
    private static final Logger LOG = LoggerFactory.getLogger(StorageAgent.class);
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final StorageConnection storageConnection;

    /**
     * Constructor
     *
     * @param storageConnection
     * @throws IOException Exception on connection problem
     */
    public StorageAgent(StorageConnection storageConnection) throws IOException {
        super();
        this.storageConnection = storageConnection;
        super.setErrorHandler(new DefaultErrorHandler(ErrorHandler.Policy.LOG|ErrorHandler.Policy.IGNORE));
    }


    @Override
    public void doHandle(SnaUpdateMessageImpl message) {
        String path = message.getPath();
        LOG.debug("storage agent informed of an update on {}...", path);
        String[] elements = UriUtils.getUriElements(path);
        String serviceProvider = elements[0];
        String service = elements[1];
        String resource = elements[2];
        JSONObject initial = message.getNotification();
        this.doHandle(serviceProvider, service, resource, initial);
    }

    @Override
    public void doHandle(SnaLifecycleMessageImpl message) {
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
    
    @Override
    public void doHandle(SnaErrorMessageImpl message) {
    }

    @Override
    public void doHandle(SnaResponseMessage<?, ?> message) {
    }

    @Override
    public void stop() {
    	super.stop();
        this.storageConnection.close();
    }
}
