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
package org.eclipse.sensinact.gateway.generic.stream;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.eclipse.sensinact.gateway.generic.TaskTranslator;

/**
 * @param <P>
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class StreamProtocolStackEndpoint<P extends Packet>
extends ProtocolStackEndpoint<P> implements StreamTaskTranslator
{
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} that will be used 
	 * 		by the StreamProtocolStackConnector to instantiate 
	 * 		to interact with the OSGi host environment
	 */
	public StreamProtocolStackEndpoint(Mediator mediator) 
	{
		super(mediator);
	}

	/**
	 * @inheritDoc
	 *
	 * @see TaskTranslator#
	 * getRequestType()
	 */
	@Override
	public Task.RequestType getRequestType()
	{
		return  REQUEST_TYPE;
	}

	/**
	 * @inheritDoc
	 *
	 * @see TaskTranslator#
	 * send(Task)
	 */
	@Override
	public void send(Task task) 
	{	
		StreamTask streamTask = (StreamTask)task;		
		send(UriUtils.getRoot(task.getPath()).substring(1), 
				streamTask.getPayloadBytesArray());
	}
}
