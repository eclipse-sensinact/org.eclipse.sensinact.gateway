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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.osgi;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModelConfiguration.BuildPolicy;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.parser.DataParser;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.thread.TemperaturesGeneratorThreadManager;
import org.osgi.framework.BundleContext;

import java.util.Collections;

public class Activator extends AbstractActivator<Mediator> {
    @Property(name = "org.eclipse.sensinact.simulated.generator.amount", defaultValue = "100")
    Integer DEVICES_NUMBER;
    private LocalProtocolStackEndpoint<TemperaturesGeneratorPacket> connector;
    private ExtModelConfiguration<TemperaturesGeneratorPacket> manager;
    private TemperaturesGeneratorThreadManager threadManager;

    public void doStart() throws Exception {
        if (manager == null) {
            manager = ExtModelConfigurationBuilder.instance(super.mediator, TemperaturesGeneratorPacket.class
        	).withResourceBuildPolicy((byte) (BuildPolicy.BUILD_NON_DESCRIBED.getPolicy() | BuildPolicy.BUILD_COMPLETE_ON_DESCRIPTION.getPolicy())
        	).withServiceBuildPolicy((byte) (BuildPolicy.BUILD_NON_DESCRIBED.getPolicy() | BuildPolicy.BUILD_ON_DESCRIPTION.getPolicy())
        	).withStartAtInitializationTime(true
        	).build("temperature-resource.xml", Collections.<String, String>emptyMap());
            manager.setObserved(Collections.singletonList("/sensor/temperature/floor"));
        }
        
        if (connector == null) {
            connector = new LocalProtocolStackEndpoint<TemperaturesGeneratorPacket>(super.mediator);
        }
        connector.connect(manager);
        DataParser dataParser = new DataParser(mediator);
        this.threadManager = new TemperaturesGeneratorThreadManager(connector, dataParser.createDeviceInfosSet(DEVICES_NUMBER));
        this.threadManager.startThreads();
    }

    public void doStop() throws Exception {
        this.threadManager.stopThreads();
        connector.stop();
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
