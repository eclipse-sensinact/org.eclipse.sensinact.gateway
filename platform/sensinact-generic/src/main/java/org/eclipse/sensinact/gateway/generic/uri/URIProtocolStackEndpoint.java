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
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.RequestType;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * @param <P>
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class URIProtocolStackEndpoint<P extends Packet> extends ProtocolStackEndpoint<P> implements URITaskTranslator {
    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} used by the StreamProtocolStackConnector to be 
     * instantiated to interact with the OSGi host environment
     */
    public URIProtocolStackEndpoint(Mediator mediator) {
        super(mediator);
    }

    @Override
    public RequestType getRequestType() {
        return REQUEST_TYPE;
    }

    @Override
    public void send(Task task) {
       URITask _task = (URITask)task;
       send(UriUtils.getRoot(_task.getPath()).substring(1), _task.getUri(), _task.getContent(), _task.getOptions());
    }
}
