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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ManagedBasisActivator;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpointConfigurator;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends ManagedBasisActivator<Mediator> {
	
    public static final String MQTT_PREFIX = "smarttopic";
	
	@Override
	protected String name() {
		return MQTT_PREFIX;
	}

	@Override
	protected ProtocolStackEndpointConfigurator configurator() {
		return new SmartTopicConfigurator();
	}

	@Override
	public Mediator doInstantiate(BundleContext context) {
		return new Mediator(context);
	}
}
