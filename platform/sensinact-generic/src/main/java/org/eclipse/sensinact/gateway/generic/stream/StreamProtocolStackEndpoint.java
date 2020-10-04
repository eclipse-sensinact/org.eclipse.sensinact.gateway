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
package org.eclipse.sensinact.gateway.generic.stream;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.SubscribeTaskWrapper;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.UnsubscribeTaskWrapper;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * @param <P>
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class StreamProtocolStackEndpoint<P extends Packet> extends ProtocolStackEndpoint<P> implements StreamTaskTranslator {
    
	/**
     * Constructor
     *
     * @param mediator the {@link Mediator} that used by the StreamProtocolStackConnector to be
     * instantiated to interact with the OSGi host environment
     */
    public StreamProtocolStackEndpoint(Mediator mediator) {
        super(mediator);
    }

    @Override
    public Task.RequestType getRequestType() {
        return REQUEST_TYPE;
    }

    @Override
    public void send(Task task) {
       StreamTask _task = (StreamTask)task;
       send(UriUtils.getRoot(_task.getPath()).substring(1), _task.getPayloadBytesArray());
    }
}
