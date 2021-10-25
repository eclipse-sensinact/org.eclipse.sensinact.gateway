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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;

/**
 * @param <P>
 */
public class EmptyPacketReader<P extends Packet> implements PacketReader<P> {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmptyPacketReader.class);
    private Mediator mediator;

    /**
     * @param mediator
     */
    protected EmptyPacketReader(Mediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void load(P packet) throws InvalidPacketException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("not implemented");
        }
    }

    @Override
    public Iterator<PayloadFragment> iterator() {
        return Collections.<PayloadFragment>emptyList().iterator();
    }

    @Override
    public void reset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("not implemented");
        }
    }

	@Override
	public void parse() throws InvalidPacketException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("not implemented");
        }
	}
}
