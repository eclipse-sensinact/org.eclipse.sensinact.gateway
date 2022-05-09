/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.DefaultPacketReaderFactory;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class DefaultConnectorCustomizer<P extends Packet> implements ConnectorCustomizer<P> {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultConnectorCustomizer.class);
    /**
     * the list of available {@link PacketReaderFactory}s
     */
    private List<PacketReaderFactory> factories;

    /**
     * the {@link ExtModelConfiguration} holding the
     * {@link Packet} type
     */
    private ExtModelConfiguration ExtModelConfiguration;

    /*
     * the {@link Mediator} allowing to interact
     * with the OSGi host environment
     */

    /**
     * Constructor
     *                              to interact with the OSGi host environment
     * @param extModelConfiguration
     * @throws InvalidPacketTypeException
     */
    public DefaultConnectorCustomizer(Mediator mediator,ExtModelConfiguration extModelConfiguration) {
        this.ExtModelConfiguration = extModelConfiguration;
        this.factories = new ArrayList<PacketReaderFactory>();

        //Mediator classloader because we don't need to retrieve
        //all declared factories in the OSGi environment, but only
        //the one specified in the bundle instantiating this BasisSnaProcessor
        ServiceLoader<PacketReaderFactory> loader = ServiceLoader.load(PacketReaderFactory.class, mediator.getClassLoader());

        Iterator<PacketReaderFactory> iterator = loader.iterator();
        while (iterator.hasNext()) {
            PacketReaderFactory factory = iterator.next();
            if (extModelConfiguration.getPacketType() == null || factory.handle(extModelConfiguration.getPacketType()))
                factories.add(factory);
        }
        if (this.factories.isEmpty())
            this.factories.add(new DefaultPacketReaderFactory(mediator, extModelConfiguration));
    }

    @Override
    public boolean preProcessing(P packet) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("pre-processing done");
        }
        return true;
    }

    @Override
    public void postProcessing(ExtServiceProviderImpl processor, PacketReader<P> packet) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("post-processing done");
        }
    }

    @Override
    public PacketReader<P> newPacketReader(P packet) throws InvalidPacketException {
        PacketReader<P> reader = null;
        Iterator<PacketReaderFactory> iterator = this.factories.iterator();

        while (iterator.hasNext()) {
            PacketReaderFactory factory = iterator.next();
            if (factory.handle(packet.getClass())) {
                reader = factory.newInstance(this.ExtModelConfiguration, packet);
                break;
            }
        }
        return reader;
    }
}
