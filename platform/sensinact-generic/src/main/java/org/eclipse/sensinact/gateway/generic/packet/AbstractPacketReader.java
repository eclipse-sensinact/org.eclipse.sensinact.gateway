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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of a {@link PacketReader}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractPacketReader<P extends Packet> implements PacketReader<P> {
    protected List<PayloadFragment> subPackets;
    protected PayloadFragment subPacket;

    /**
     * Constructor
     */
    protected AbstractPacketReader() {
        this.subPackets = new ArrayList<PayloadFragment>();
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<PayloadFragment> iterator() {
        return Collections.<PayloadFragment>unmodifiableList(this.subPackets).iterator();
    }

    /**
     * @inheritDoc
     * @see PacketReader#getTaskIdValuePairs()
     */
    public Iterator<TaskIdValuePair> getTaskIdValuePairs() {
        List<TaskIdValuePair> taskResults = new ArrayList<TaskIdValuePair>();

        int index = 0;
        int length = this.subPackets == null ? 0 : this.subPackets.size();
        for (; index < length; index++) {
            taskResults.addAll(this.subPackets.get(index).getTaskIdValuePairs());
        }
        return taskResults.iterator();
    }

    /**
     * Add the {@link PayloadFragment} passed as parameter to the list
     * of those of the parsed {@link Packet}
     *
     * @param packet the {@link PayloadFragment} to add
     */
    protected void addSubPacket(PayloadFragment subPacket) {
        if (subPacket == null) {
            return;
        }
        this.subPackets.add(subPacket);
    }
}
