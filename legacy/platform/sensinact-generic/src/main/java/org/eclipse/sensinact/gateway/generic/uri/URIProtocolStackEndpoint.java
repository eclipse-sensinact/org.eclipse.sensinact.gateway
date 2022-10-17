/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.uri;

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
     */
    public URIProtocolStackEndpoint() {
        super();
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
