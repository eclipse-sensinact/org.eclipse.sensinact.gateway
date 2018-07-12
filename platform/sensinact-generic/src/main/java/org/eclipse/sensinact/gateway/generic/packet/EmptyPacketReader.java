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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import java.util.Collections;
import java.util.Iterator;

/**
 * @param <P>
 */
public class EmptyPacketReader<P extends Packet> implements PacketReader<P> {
    private Mediator mediator;

    /**
     * @param mediator
     */
    protected EmptyPacketReader(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see PacketReader#
     * parse(Packet)
     */
    @Override
    public void parse(P packet) throws InvalidPacketException {
        if (this.mediator.isDebugLoggable()) {
            this.mediator.debug("not implemented");
        }
    }

    /**
     * @inheritDoc
     * @see PacketReader#getTaskIdValuePairs()
     */
    @Override
    public Iterator<TaskIdValuePair> getTaskIdValuePairs() {
        if (this.mediator.isDebugLoggable()) {
            this.mediator.debug("not implemented");
        }
        return Collections.<TaskIdValuePair>emptyList().iterator();
    }

    /**
     * @inheritDoc
     * @see PacketReader#treated(java.lang.String)
     */
    @Override
    public void treated(String taskIdentifier) {
        if (this.mediator.isDebugLoggable()) {
            this.mediator.debug("not implemented");
        }
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<PayloadFragment> iterator() {
        return Collections.<PayloadFragment>emptyList().iterator();
    }

    /**
     * @inheritDoc
     * @see PacketReader#reset()
     */
    @Override
    public void reset() {
        if (this.mediator.isDebugLoggable()) {
            this.mediator.debug("not implemented");
        }
    }
}
