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
package org.eclipse.sensinact.gateway.nthbnd.ldap.filter.test;

import org.eclipse.sensinact.gateway.common.bundle.AbstractActivator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.ExtModelInstanceBuilder;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Collections;

public class Activator extends AbstractActivator<Mediator> {
    private LocalProtocolStackEndpoint<Packet> connector;

    public void doStart() throws Exception {
        ExtModelConfiguration<Packet> manager = ExtModelConfigurationBuilder.instance(super.mediator
        	).withStartAtInitializationTime(true
        	).withObserved(new ArrayList<String>() {{
        		this.add("/service1/humidity/accessible");
        		this.add("/service1/temperature");
        	}}).build("resources.xml", Collections.<String, String>emptyMap());

        connector = new LocalProtocolStackEndpoint<Packet>(super.mediator);
        connector.connect(manager);
    }

    public void doStop() throws Exception {
        connector.stop();
        connector = null;
    }

    public Mediator doInstantiate(BundleContext context) {
        return new Mediator(context);
    }
}
