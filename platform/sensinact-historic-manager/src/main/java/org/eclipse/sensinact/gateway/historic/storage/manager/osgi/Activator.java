/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.historic.storage.manager.osgi;

import org.eclipse.sensinact.gateway.generic.BasisActivator;
import org.eclipse.sensinact.gateway.generic.annotation.SensiNactBridgeConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.Packet;

/**
 * Handle the bundle activation/deactivation
 */
@SensiNactBridgeConfiguration(outputOnly = true)
public class Activator extends BasisActivator<Packet> {}
