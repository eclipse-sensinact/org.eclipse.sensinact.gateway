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

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a {@link PacketReader}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractPacketReader<P extends Packet> implements PacketReader<P> {
    
	private static final Logger LOG = LoggerFactory.getLogger(PacketReader.class);
	
	protected CountDownLatch countDown;
	protected ExecutorService worker;
    protected PayloadFragment subPacket;

    /**
     * Constructor
     */
    protected AbstractPacketReader() {
    	this.countDown = new CountDownLatch(1);
    	this.worker = Executors.newSingleThreadExecutor();
    }

    @Override
    public Iterator<PayloadFragment> iterator() {
    	return new Iterator<PayloadFragment>() {
    		
    		private void parse (){
    			AbstractPacketReader.this.worker.submit(new Runnable() {
					@Override
					public void run() {
						try {
							AbstractPacketReader.this.parse();
						} catch (InvalidPacketException e) {
							LOG.error(e.getMessage(),e);
						}
					}
    				
    			});
    		}
    		
    		private void await()  {
				try {
					AbstractPacketReader.this.countDown.await();
					AbstractPacketReader.this.countDown = new CountDownLatch(1);
				} catch (InterruptedException e) {
					Thread.interrupted();
					AbstractPacketReader.this.setSubPacket(PayloadFragment.EOF_FRAGMENT);
				}
    		}
    		
			@Override
			public boolean hasNext() {
				if(AbstractPacketReader.this.subPacket == null) {	
					parse();
					await();
				}
				return PayloadFragment.EOF_FRAGMENT != AbstractPacketReader.this.subPacket;
			}

			@Override
			public PayloadFragment next() {
				if(AbstractPacketReader.this.subPacket == null) {
					parse();
					await();
				}
				if(PayloadFragment.EOF_FRAGMENT == AbstractPacketReader.this.subPacket)
					return null;
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
        this.countDown.countDown();
    }
}
