/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.mid;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClient;
import org.eclipse.sensinact.gateway.protocol.http.client.mid.MidClientListener;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Extended abstract {@link BasisDeviceURIFactory} dedicated to devices using
 * the HTTP protocol
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class MidHttpProtocolStackEndpoint<RESPONSE extends HttpResponse, REQUEST extends MidHttpRequest<RESPONSE>> extends HttpProtocolStackEndpoint implements MidClientListener<RESPONSE> {
	
	private static final Logger LOG = LoggerFactory.getLogger(MidHttpProtocolStackEndpoint.class);
    /**
     * the intermediate client
     */
    protected final MidClient<RESPONSE, REQUEST> client;

    /**

     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public MidHttpProtocolStackEndpoint() throws ParserConfigurationException, SAXException, IOException {
        super();
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
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
