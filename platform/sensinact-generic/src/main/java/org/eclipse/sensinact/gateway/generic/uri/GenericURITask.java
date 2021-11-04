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
package org.eclipse.sensinact.gateway.generic.uri;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.TaskImpl;

/**
 * A generic {@link Task} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class GenericURITask extends TaskImpl implements URITask {
    /**
     * Constructor
     *
     * @param mediator       the {@link Mediator} allowing to interact with the
     *                       OSGi hot environment
     * @param transmitter    the {@link TaskTranslator} executing the {@link Task}
     *                       to be instantiated
     * @param path           the string path of the requirer {@link ModelElement}
     * @param profileId      the string profile identifier of the {@link ModelInstance}
     *                       to which the requirer {@link ModelElement} belongs
     * @param resourceConfig the {@link ExtResourceConfig} configuring the
     *                       requirer {@link Resource} if it applies
     * @param parameters     the objects parameter parameterizing the task
     *                       execution
     */
    public GenericURITask( CommandType command, URITaskTranslator transmitter, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(command, transmitter, path, profileId, resourceConfig, parameters);
    }

    /**
     * @inheritDoc
     * @see Task#getRequestType()
     */
    @Override
    public Task.RequestType getRequestType() {
        return REQUEST_TYPE;
    }
}
