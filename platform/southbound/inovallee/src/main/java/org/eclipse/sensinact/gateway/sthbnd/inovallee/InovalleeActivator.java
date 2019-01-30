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

import org.eclipse.sensinact.gateway.generic.GenericActivator;

/**
 * sensiNact bundle activator
 */
public abstract class InovalleeActivator extends GenericActivator {

    @Override
    public InovalleeProtocolStackEndpoint getEndPoint() {
        return new InovalleeProtocolStackEndpoint(mediator);
    }

    public Class getPacketClass(){
        return InovalleePacket.class;
    }
}
