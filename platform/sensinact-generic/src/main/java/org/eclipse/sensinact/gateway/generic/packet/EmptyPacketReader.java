/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.packet;

import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <P>
 */
public class EmptyPacketReader<P extends Packet> implements PacketReader<P> {
	
	private static final Logger LOG = LoggerFactory.getLogger(EmptyPacketReader.class);


    protected EmptyPacketReader() {
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
