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
package org.eclipse.sensinact.gateway.sthbnd.http.mid;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.Reusable;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTaskImpl;

/**
 * Extended {@link URITask} dedicated to HTTP communication
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class MidHttpTask<RESPONSE extends HttpResponse, REQUEST extends MidHttpRequest<RESPONSE>> 
extends HttpTaskImpl<RESPONSE, REQUEST> implements Reusable {
    /**
     * Constructor
     *
     * @param mediator       the associated {@link Mediator}
     * @param transmitter    the {@link TaskTranslator} in charge of sending the requests
     *                       based on the task to instantiate
     * @param path           String path of the {@link SnaObject} which has created the task
     *                       to instantiate
     * @param resourceConfig the {@link ResourceConfig} mapped to the {@link ExtResourceImpl}
     *                       on which the task applies
     * @param parameters     the objects array parameterizing the call
     */
    public MidHttpTask(Mediator mediator, CommandType command, TaskTranslator transmitter, Class<REQUEST> requestType, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(mediator, command, transmitter, requestType, path, profileId, resourceConfig, parameters);
    }
}
