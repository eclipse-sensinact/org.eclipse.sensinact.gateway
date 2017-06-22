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

package org.eclipse.sensinact.gateway.sthbnd.http;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.sensinact.gateway.generic.*;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpDiscoveryTask;
import org.xml.sax.SAXException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task.RequestType;
import org.eclipse.sensinact.gateway.protocol.http.Headers;
import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.eclipse.sensinact.gateway.protocol.http.client.Request;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

/**
 * Extended abstract {@link ProtocolStackEndpoint} dedicated to devices using
 * the HTTP protocol
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class HttpProtocolStackEndpoint extends ProtocolStackEndpoint<HttpPacket>
{	
//	/**
//	 * HttpPackets stack
//	 */
//	private final HttpPacketStack stack;

	/**
	 * permanent header fields added to each request
	 */
	protected Headers permanentHeaders;
	
	/**
	 * the {@link HttpDiscoveryTask}s executed at connection time
	 */
	protected Deque<HttpDiscoveryTask<?,?>> discovery;
	
	/**
	 * the {@link HttpDiscoveryTask}s executed at connection time
	 */
	protected Deque<HttpTask<?,?>> disconnexion;
	
	/**
	 * this HttpProtocolStackEndpoint's stopping status
	 */
	private boolean stopping;

	/**
	 * 
	 */
	protected Class<? extends HttpPacket> packetType;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public HttpProtocolStackEndpoint(Mediator mediator)
			throws ParserConfigurationException, SAXException, IOException
	{
		super(mediator);
		this.stopping = false;
		
		this.permanentHeaders = new HeadersCollection();
		this.discovery = new LinkedList<HttpDiscoveryTask<?,?>>();
		this.disconnexion = new LinkedList<HttpTask<?,?>>();
		
//		this.stack = new HttpPacketStack();
//		new Thread(this.stack).start();
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see TaskTranslator#
	 * send(Task)
	 */
	@SuppressWarnings("unchecked")
	public void send(Task task)
	{
		HttpTask<HttpResponse,Request<HttpResponse>> httpTask = 
			(HttpTask<HttpResponse,Request<HttpResponse>>)task;
		
		httpTask.addHeaders(this.permanentHeaders.getHeaders());
		if(httpTask.getPacketType() == null)
		{
			httpTask.setPacketType(packetType);
		}
		try 
		{
			Request<HttpResponse> request = httpTask.build();
			HttpResponse response = request.send();
			
			if(response == null)
			{
				mediator.error("Unable to connect");
				return;
			}
	    	if(!httpTask.isDirect())
			{
				HttpPacket packet = response.createPacket();
				this.process(packet);
	            
			} else
			{
				task.setResult(response.getContent());
			}
		} catch (Exception e) 
		{
			super.mediator.error(e);
		}
	}	
	
	/**
	 * @inheritDoc
	 * 
	 * @see ProtocolStackEndpoint#
	 * connect(ExtModelConfiguration)
	 */
	@SuppressWarnings("unchecked")
	public void connect(ExtModelConfiguration manager) 
			throws InvalidProtocolStackException
	{
		this.packetType = (Class<? extends HttpPacket>)
				manager.getPacketType();
		
		super.connect(manager);		
		
		Iterator<HttpDiscoveryTask<?,?>> iterator = 
				this.discovery.iterator();
		
		while(iterator.hasNext())
		{
			HttpDiscoveryTask<?,?> discoveryTask = iterator.next();
			if (discoveryTask != null)
			{		
				super.connector.execute(discoveryTask);
				long wait = discoveryTask.getTimeout();
				while (!discoveryTask.isResultAvailable() && wait > 0)
				{
					try
					{
						Thread.sleep(150);
						wait -= 150;
					}
					catch (InterruptedException e)
					{
						Thread.interrupted();
						if (this.mediator.isErrorLoggable())
						{
							this.mediator.error(e.getMessage(),e);
						}
						break;
					}
				}
			}
		}
	} 
    
//	/**
//	 * Stacks the {@link HttpPacket} passed as parameter
//	 * for a future processing
//	 * 
//	 * @param packet
//	 * 		the {@link HttpPacket} to stack
//	 */
//	@Override
//	public void process(HttpPacket packet) 
//    		throws InvalidPacketException
//    {
//		this.stack.addPacket(packet);
//    }
//	
//	/**
//	 * Processes the {@link HttpPacket} passed as parameter
//	 * 
//	 * @param packet
//	 * 		the {@link HttpPacket} to process
//	 */
//	protected final void doProcess(HttpPacket packet)
//			throws InvalidPacketException
//	{
//    	super.process(packet);
//	}
	
	/**
	 * Registers a permanent header field value that will be added to 
	 * each request build by this HttpProtocolStackEndpoint
	 * 
	 * @param header
	 * 		header field name for which to add a permanent value 
	 * @param value
	 * 		the permanent header field value to add
	 */
	public void registerPermanentHeader(String header, String value)
	{
		if(header != null && value != null)
		{
			this.permanentHeaders.addHeader(header, value);
		}
	}
	
	/**
	 * Registers an {@link HttpDiscoveryTask} to this HttpProtocolStackEndpoint
	 * the registered {@link HttpDiscoveryTask}s are the first executed at 
	 * connection time
	 * 
	 * @param task
	 * 		 the {@link HttpDiscoveryTask} to register
	 */
	public void registerDiscoveryTask(HttpDiscoveryTask<?,?> task)
	{
		if(task != null)
		{
			this.discovery.add(task);
		}
	}

	/**
	 * Registers an {@link HttpTask} to this HttpProtocolStackEndpoint
	 * the registered {@link HttpTask}s are the last executed at 
	 * stopping time
	 * 
	 * @param task the {@link HttpTask} to register
	 */
	public void registerDisconnexionTask(HttpTask<?,?> task)
	{
		if(task != null)
		{
			this.disconnexion.add(task);
		}
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see TaskTranslator#getRequestType()
	 */
	@Override
	public RequestType getRequestType()
	{
		return RequestType.URI;
	}

	/**
	 * Stops this {@link ProtocolStackEndpoint} and its
	 * associated {@link Connector}
	 */
    public void stop()
    {
    	this.stopping = true;
		Iterator<HttpTask<?,?>> iterator = 
				this.disconnexion.iterator();		
		while(iterator.hasNext())
		{
			HttpTask<?,?> disconnexionTask = iterator.next();
			if (disconnexionTask != null)
			{		
				super.connector.execute(disconnexionTask);
				long wait = disconnexionTask.getTimeout();
				while (!disconnexionTask.isResultAvailable() 
						&& wait > 0)
				{
					try
					{
						Thread.sleep(150);
						wait -= 150;
					}
					catch (InterruptedException e)
					{
						Thread.interrupted();
						if (this.mediator.isErrorLoggable())
						{
							this.mediator.error(e.getMessage(),e);
						}
						break;
					}
				}
			}
		}
    	if(super.connector != null)
    	{
    		super.connector.stop();
    	}
    } 
    
//	/**
//	 * Stack of {@link HttpPacket} waiting to be processed
//	 */
//	private final class HttpPacketStack implements Runnable 
//	{
//		private final AtomicBoolean running;
//		private LinkedList<HttpPacket> packets;
//
//		/**
//		 * Constructor
//		 */
//		HttpPacketStack()
//		{
//			this.running = new AtomicBoolean(false);
//			this.packets = new LinkedList<HttpPacket>();
//		}
//
//		/**
//		 * Adds the {@link HttpPacket} passed as parameter 
//		 * on the top of the managed list
//		 * 
//		 * @param packet
//		 *     the {@link HttpPacket} to add
//		 */
//		public void addPacket(HttpPacket request) 
//		{
//			synchronized (this)
//			{
//				this.packets.offer(request);
//			}
//		}
//
//		/**
//		 * Removes and  returns the {@link HttPacket} 
//		 * from the head of the list
//		 * 
//		 * @return 
//		 *     list head {@link HttPacket}
//		 */
//		private HttpPacket getPacket()
//		{
//			HttpPacket packet = null;
//			synchronized (this) 
//			{
//				packet = this.packets.pollFirst();
//			}
//			return packet;
//		}
//
//		/**
//		 * @InheritDoc
//		 *
//		 * @see java.lang.Runnable#run()
//		 */
//		@Override
//		public void run()
//		{
//			running(true);
//			
//			while (running()) 
//			{
//				HttpPacket packet = getPacket();
//				if (packet == null)
//				{
//					try 
//					{
//						Thread.sleep(100);
//
//					} catch (InterruptedException e)
//					{
//						Thread.interrupted();
//						HttpProtocolStackEndpoint.this.mediator.error(e);
//						this.running(false);
//					}
//					continue;
//				}
//				try 
//				{
//					HttpProtocolStackEndpoint.this.doProcess(packet);
//
//				} catch (InvalidPacketException e)
//				{
//					HttpProtocolStackEndpoint.this.mediator.error(e);
//				}
//			}
//		}
//
//		/**
//		 * Defines the running state
//		 * 
//		 * @param running
//		 *            the running state
//		 */
//		void running(boolean running) 
//		{
//			synchronized (this)
//			{
//				this.running.set(running);
//			}
//		}
//
//		/**
//		 * Returns the running state
//		 * 
//		 * @return the running state
//		 */
//		boolean running()
//		{
//			boolean running = false;
//			synchronized (this)
//			{
//				running = this.running.get();
//			}
//			return running;
//		}
//	}
}
