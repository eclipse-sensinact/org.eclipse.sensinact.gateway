/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPDiscoveryListenerItf;
import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPDiscoveryNotifierItf;
import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPEvent;
import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPDescriptionPacket;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KodiDevicesDiscovery implements SSDPDiscoveryListenerItf {
	
	private static final Logger LOG = LoggerFactory.getLogger(KodiDevicesDiscovery.class);
    private Mediator mediator;
    private SimpleHttpProtocolStackEndpoint connector;

    public KodiDevicesDiscovery(Mediator mediator, final SimpleHttpProtocolStackEndpoint connector, final String kodiPattern) {
        this.mediator = mediator;
        this.connector = connector;
        mediator.attachOnServiceAppearing(SSDPDiscoveryNotifierItf.class, null, new Executable<SSDPDiscoveryNotifierItf, Void>() {
            @Override
            public Void execute(SSDPDiscoveryNotifierItf notifier) throws Exception {
                String kodiPattern = (String) KodiDevicesDiscovery.this.mediator.getProperty("kodi.regex");
                notifier.addListener(KodiDevicesDiscovery.this, kodiPattern);
                List<SSDPDescriptionPacket> descriptionPackets = notifier.getDescriptions(kodiPattern);
                for (SSDPDescriptionPacket descriptionPacket : descriptionPackets) {
                    KodiDevicesDiscovery.this.eventSSDP(SSDPEvent.DISCOVER, descriptionPacket);
                }
                return null;
            }
        });
    }

    public void eventSSDP(SSDPEvent ssdpEvent, SSDPDescriptionPacket packet) {
        LOG.debug("New SSDP event (" + ssdpEvent + "): " + packet.toString());
        String serviceProvider = packet.getFriendlyName().replace("(", "").replace(")", "");
        String url = "http://" + packet.getUrl() + "/jsonrpc";
        try {
            connector.process(new KodiRequestPacket(serviceProvider, ServiceProvider.ADMINISTRATION_SERVICE_NAME, "jsonurl", url));

        } catch (InvalidPacketException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
