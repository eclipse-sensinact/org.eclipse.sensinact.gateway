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
package org.eclipse.sensinact.gateway.sthbnd.http.mid;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClient;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClientListener;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Extended abstract {@link BasisDeviceURIFactory} dedicated to devices using
 * the HTTP protocol
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class MidHttpProtocolStackEndpoint<RESPONSE extends HttpResponse, REQUEST extends MidHttpRequest<RESPONSE>> extends HttpProtocolStackEndpoint implements MidClientListener<RESPONSE> {
    /**
     * the intermediate client
     */
    protected final MidClient<RESPONSE, REQUEST> client;

    /**
     * @param mediator
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public MidHttpProtocolStackEndpoint(Mediator mediator) throws ParserConfigurationException, SAXException, IOException {
        super(mediator);
        this.client = new MidClient<RESPONSE, REQUEST>(this);
    }

    @Override
    public void send(Task task) {
        if (MidHttpTask.class.isAssignableFrom(task.getClass())) {
            MidHttpTask<RESPONSE, REQUEST> httpMidtask = (MidHttpTask<RESPONSE, REQUEST>) task;
            httpMidtask.addHeaders(this.permanentHeaders.getHeaders());

            REQUEST request = httpMidtask.build();
            this.client.addRequest(request);

        } else {
            super.send(task);
        }

    }

    @Override
    public void respond(RESPONSE response) {
        if (!response.isDirect()) {
            try {
                super.process(response.createPacket());
            } catch (InvalidPacketException e) {
                this.mediator.error(e);
            }
        }
    }
}
