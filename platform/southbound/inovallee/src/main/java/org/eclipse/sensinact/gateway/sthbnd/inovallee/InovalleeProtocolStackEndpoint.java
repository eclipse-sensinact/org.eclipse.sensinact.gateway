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
package org.eclipse.sensinact.gateway.sthbnd.inovallee;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InovalleeProtocolStackEndpoint extends LocalProtocolStackEndpoint<InovalleePacket> {
    private static final Logger LOG = LoggerFactory.getLogger(InovalleeProtocolStackEndpoint.class);

    public InovalleeProtocolStackEndpoint(Mediator mediator) {
        super(mediator);
    }
}
