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
package org.eclipse.sensinact.gateway.system.invoker;

import java.util.Collections;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component
public class Activator {
	
	// Not really needed but required for the lifecycle otherwise the
	// connector#connect call will NPE due to a missing security model
	@Reference
	Core core;
	
    private ExtModelConfiguration<Packet> manager = null;
    private LocalProtocolStackEndpoint<Packet> connector = null;

    @Activate
    void start(BundleContext ctx) throws Exception {
    	Mediator mediator = new Mediator(ctx);
        if (manager == null) {
            manager = ExtModelConfigurationBuilder.instance(mediator
            ).withStartAtInitializationTime(true
            ).build("system-resource.xml", Collections.<String, String>emptyMap());
        }
        if (this.connector == null) {
            this.connector = new LocalProtocolStackEndpoint<Packet>(mediator);
        }
        this.connector.connect(manager);
    }

    @Deactivate
    void stop() {
        if (this.connector != null) {
            this.connector.stop();
            this.connector = null;
        }
        this.manager = null;
    }
}
