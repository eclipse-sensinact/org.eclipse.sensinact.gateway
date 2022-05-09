/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
