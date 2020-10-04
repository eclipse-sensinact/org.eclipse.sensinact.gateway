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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ResourceConfig;

public class ExtModelInstance<C extends ExtModelConfiguration> extends ModelInstance<C> {
    /**
     * the {@link TaskManager} to which the {@link Task}s
     * processing is delegated
     */
    protected final TaskManager taskManager;

    /**
     * Constructor
     *
     * @param mediator           the {@link Mediator} allowing to interact
     *                           with the OSGi host environment
     * @param modelConfiguration the {@link ModelConfiguration}
     *                           applying on the {@link ModelInstance} to be instantiated
     * @param name               the root name of the {@link ModelInstance} to
     *                           be instantiated
     * @param processor          the {@link TaskManager} making the link
     *                           between model element and remote connected counterpart
     * @throws InvalidServiceProviderException if an error occurred
     *                                         while instantiating the new {@link ModelInstance}
     */
    public ExtModelInstance(Mediator mediator, C modelConfiguration, String name, TaskManager processor) throws InvalidServiceProviderException {
        this(mediator, modelConfiguration, name, null, processor);
    }

    /**
     * Constructor
     *
     * @param mediator           the {@link Mediator} allowing to interact
     *                           with the OSGi host environment
     * @param modelConfiguration the {@link ModelConfiguration}
     *                           applying on the {@link ModelInstance} to be instantiated
     * @param name               the root name of the {@link ModelInstance} to
     *                           be instantiated
     * @param profile            the root profile of the {@link ModelInstance}
     *                           to be instantiated
     * @param processor          the {@link TaskManager} making the link
     *                           between model element and remote connected counterpart
     * @throws InvalidServiceProviderException if an error occurred
     *                                         whil instantiating the new {@link ModelInstance}
     */
    public ExtModelInstance(Mediator mediator, C modelConfiguration, String name, String profileId, TaskManager processor) throws InvalidServiceProviderException {
        super(mediator, modelConfiguration, name, profileId);
        this.taskManager = processor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ExtServiceProviderImpl getRootElement() {
        return (ExtServiceProviderImpl) super.provider;
    }

    /**
     * Delegates the execution of the method whose type
     * is passed as parameter for the {@link Resource}
     * object mapped to the {@link ResourceConfig} also
     * passed as parameter, using the <code>'parameters
     * '</code> argument to parameterize the call. This
     * method propagates an invocation of method to a
     * connected device or a remote service
     *
     * @param command        the command to execute
     * @param path           the string path of the target ModelElement
     * @param resourceConfig the {@link ResourceConfig} associated to the
     *                       {@link Resource} for which the method is called
     * @param parameters     array of objects used to parameterize the call
     * @return The created and executed {@link Task}
     */
    public Task propagate(Task.CommandType command, String path, ResourceConfig resourceConfig, Object[] parameters) {
        return this.taskManager.execute(command, path, super.profileId, resourceConfig, parameters);
    }
}
