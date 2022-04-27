/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.historic.storage.manager.osgi;

import org.eclipse.sensinact.gateway.generic.BasisActivator;
import org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.osgi.annotation.bundle.Header;
import org.osgi.framework.Constants;

/**
 * Handle the bundle activation/deactivation
 */
@SensiNactBridgeConfiguration(outputOnly = true)
@Header(name = Constants.BUNDLE_ACTIVATOR, value = "${@class}")
public class Activator extends BasisActivator<Packet> {}
