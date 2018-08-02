/*
 * Copyright (c) 2018 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.json.JSONObject;

public class OneM2MHttpPacketReader extends SimplePacketReader<HttpPacket> {
    public static final String DEFAULT_SERVICE_NAME = "container";

    /**
     * @param mediator the mediator of the bundle
     */
    public OneM2MHttpPacketReader(Mediator mediator) {
        super(mediator);
    }

    /**
     * @inheritDoc
     * @see PacketReader#parse(Packet)
     */
    @Override
    public void parse(HttpPacket packet) throws InvalidPacketException {
        try {
            JSONObject content = new JSONObject(new String(packet.getBytes()));
            if (mediator.isDebugLoggable()) {
                mediator.debug(content.toString());
            }
            if (content.has("m2m:uril")) {
                String[] uris = content.getString("m2m:uril").split(" ");
                for (String uri : uris) {
                    String[] elements = uri.split("/");
                    if (elements.length >= 3) {
                        if (elements.length >= 5 && elements.length < 6) {
                            super.setResourceId(elements[4]);
                            super.setServiceId(elements[3]);
                        } else if ("admin".equalsIgnoreCase(elements[3])) {
                            super.setServiceId(elements[3]);
                        } else {
                            super.setResourceId(elements[3]);
                            super.setServiceId(DEFAULT_SERVICE_NAME);
                        }
                        super.setServiceProviderId(elements[2]);
                        super.configure();
                    }
                }
            }
        } catch (Exception e) {
            mediator.error(e);
            throw new InvalidPacketException(e);
        }
    }
}