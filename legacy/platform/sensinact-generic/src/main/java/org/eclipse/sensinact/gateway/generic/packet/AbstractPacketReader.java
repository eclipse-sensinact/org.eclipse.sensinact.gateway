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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a {@link PacketReader}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractPacketReader<P extends Packet> implements PacketReader<P> {
    
	private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);

    protected PayloadFragment subPacket;

    /**
     * Constructor
     */
    protected AbstractPacketReader() {
    }

    @Override
    public Iterator<PayloadFragment> iterator() {
    	return new Iterator<PayloadFragment>() {
    		
			@Override
			public boolean hasNext() {
				if(AbstractPacketReader.this.subPacket == null) {	
					try {
						parse();
						if(subPacket == null) {
							LOG.error("Parsing a packet did not return a fragment");
							setSubPacket(PayloadFragment.EOF_FRAGMENT);
						}
					} catch (InvalidPacketException e) {
						LOG.error("An exception occurred parsing a packet",e); 
						setSubPacket(PayloadFragment.EOF_FRAGMENT);
					}
				}
				return PayloadFragment.EOF_FRAGMENT != AbstractPacketReader.this.subPacket;
			}

			@Override
			public PayloadFragment next() {
				if(!hasNext()) {
					throw new NoSuchElementException("There are no more sub-packets");
				}
				PayloadFragment fragment = AbstractPacketReader.this.subPacket;
				AbstractPacketReader.this.subPacket = null;
				return fragment;
			}
    	};
    }

    /**
     * Add the {@link PayloadFragment} passed as parameter to the list
     * of those of the parsed {@link Packet}
     *
     * @param packet the {@link PayloadFragment} to add
     */
    protected void setSubPacket(PayloadFragment subPacket) {
        if (subPacket == null) 
            return;
        this.subPacket = subPacket;
    }
}
