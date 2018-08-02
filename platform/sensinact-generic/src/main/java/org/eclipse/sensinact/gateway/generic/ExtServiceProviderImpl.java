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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.UriUtils;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

/**
 * Extended abstract {@link ServiceProviderImpl} implementation
 * to reify a ServiceProvider in the gateway
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ExtServiceProviderImpl extends ServiceProviderImpl {
    /**
     * Constructor
     *
     * @param context the {@link BundleContext} where to register the service in
     * @throws InvalidServiceProviderException
     * @throws InvalidValueException
     */
    protected ExtServiceProviderImpl(ExtModelInstance<?> modelInstance, String name) throws InvalidServiceProviderException {
        this(modelInstance, name, Collections.<String>emptyList());
    }

    /**
     * Constructor
     *
     * @param context the {@link BundleContext} where to register the service in
     * @throws InvalidServiceProviderException
     * @throws InvalidValueException
     */
    protected ExtServiceProviderImpl(ExtModelInstance<?> modelInstance, String name, List<String> serviceNames) throws InvalidServiceProviderException {
        super(modelInstance, name, serviceNames);
    }

    /**
     * @inheritDoc
     * @see ModelElement#
     * passOn(AccessMethod.Type,
     * java.lang.String, java.lang.Object[])
     */
    @Override
    protected Task passOn(String type, String path, Object[] parameters) throws Exception {
        String[] pathElements = UriUtils.getUriElements(path);
        String service = null;
        String resource = null;

        if (pathElements.length > 2) {
            service = pathElements[1];
            resource = pathElements[2];
        }
        Task.CommandType command = Task.CommandType.valueOf(type);
        ResourceConfig resourceConfig = null;

        resourceConfig = ((ExtModelInstance<?>) super.modelInstance).configuration().getResourceConfig(new ExtResourceDescriptor().withServiceName(service).withResourceName(resource));

        Task task = ((ExtModelInstance<?>) super.modelInstance).propagate(command, path, resourceConfig, parameters);

        if (task != null) {
            long wait = task.getTimeout();
            while (!task.isResultAvailable() && wait > 0) {
                try {
                    Thread.sleep(150);
                    wait -= 150;
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    ExtServiceProviderImpl.super.modelInstance.mediator().error(e);
                    break;
                }
            }
            if (!task.isResultAvailable()) {
                task.abort(AccessMethod.EMPTY);
            }
        }
        return task;
    }

    /**
     * Completes the starting process of this service provider
     *
     * @throws Exception
     */
    @Override
    protected void doStart() throws Exception {
        Task task = ((ExtModelInstance<?>) super.modelInstance).propagate(Task.CommandType.SERVICES_ENUMERATION, this.getPath(), null, null);

        if (task == null) {
            super.doStart();
            return;
        }
        task.registerCallBack(new TaskCallBack(new Executable<Task, Void>() {
            @Override
            public Void execute(Task task) throws Exception {
                if (task.isResultAvailable()) {
                    Object serviceIds = task.getResult();
                    Class<?> componentType = null;

                    if (serviceIds != null && (componentType = serviceIds.getClass().getComponentType()) != null && componentType == String.class) {
                        int length = Array.getLength(serviceIds);
                        int index = 0;
                        for (; index < length; index++) {
                            String serviceId = (String) Array.get(serviceIds, index);
                            ExtServiceProviderImpl.super.serviceNames.add(serviceId);
                        }
                    }
                } else {
                    task.abort(AccessMethod.EMPTY);
                    throw new InvalidServiceProviderException("services enumeration task timeout");
                }
                ExtServiceProviderImpl.super.doStart();
                return null;
            }
        }));
    }

}
