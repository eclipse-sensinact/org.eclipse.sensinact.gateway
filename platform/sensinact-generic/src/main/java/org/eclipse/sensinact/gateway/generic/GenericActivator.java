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
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.List;

public abstract class GenericActivator extends AbstractActivator<Mediator> {

    protected LocalProtocolStackEndpoint endPoint;

    @Override
    public void doStart() throws Exception {
    	ExtModelConfigurationBuilder builder = ExtModelConfigurationBuilder.instance(mediator, getPacketClass());
    	
    	ExtModelConfiguration configuration = configureBuilder(builder)
        	.withServiceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()))
        	.withResourceBuildPolicy((byte) (SensiNactResourceModelConfiguration.BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy() | SensiNactResourceModelConfiguration.BuildPolicy.BUILD_NON_DESCRIBED.getPolicy()))
        	.withStartAtInitializationTime(true)
        	.build(this.getClass().getName()+".xml", Collections.emptyMap());
        endPoint = getEndPoint();
        this.connect(configuration);
    }

    protected ExtModelConfigurationBuilder configureBuilder(ExtModelConfigurationBuilder builder) {
    	return builder;
    }
    
    @Override
    public void doStop() {
        try {
            endPoint.stop();
        } finally {
            endPoint = null;
        }
    }

    /**
     * @param configuration the configuration for the bridge
     * @throws InvalidProtocolStackException
     */
    protected void connect( ExtModelConfiguration<?> configuration) throws InvalidProtocolStackException {
        endPoint.connect(configuration);

    }

    public LocalProtocolStackEndpoint getEndPoint() {
        return endPoint;
    }

    @Override
    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }

    public abstract Class getPacketClass();

	protected void processPackets(List<? extends Packet> packets) throws InvalidPacketException {
		for (Packet p : packets)
			endPoint.process(p);
	}   
}
