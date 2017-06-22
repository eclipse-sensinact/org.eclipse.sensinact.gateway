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
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * A callback recipient
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Recipient extends JSONable
{
	static final String RECIPIENT_JSONSCHEMA = "";
	
	/**
	 * Returns the type of the {@link SnaCallBack}
	 * associated to this recipient
	 *  
	 * @return
	 * 		the type of the {@link SnaCallBack} 
	 * 		associated to this recipient
	 */
	SnaCallback.Type getSnaCallBackType();

//	/**
//	 * Returns true if this Recipient requires to
//	 * be notified even if the value of an updated
//	 * resource did not change 
//	 * 
//	 * @return
//	 * 		<ul>
//	 * 			<li>true if the unchanged value notification
//	 * 			is required</li>
//	 * 			<li>false otherwise</li>
//	 * 		</ul>
//	 */
//	boolean handleUnchanged();
	
	/**
	 * Returns the lifetime of the {@link SnaCallBack}
	 * associated to this recipient
	 * 
	 * @return
	 * 		the lifetime of the {@link SnaCallBack}
	 * 		associated to this recipient
	 */
	long getLifetime();	

	/**
	 * Returns the size of the buffer of the
	 * {@link BufferCallBack} associated to this 
	 * recipient
	 * 
	 * @return
	 * 		the size of the buffer of the {@link 
	 * 		BufferCallBack} associated to this 
	 * 		recipient
	 */
	int getBufferSize();
	
	/**
	 * Returns the delay of the scheduler of the
	 * {@link ScheduledCallBack} associated to this 
	 * recipient
	 * 
	 * @return
	 * 		the delay of the scheduler of the
	 * 		{@link ScheduledCallBack} associated 
	 * 		to this recipient
	 */
	int getSchedulerDelay();
	
	/**
	 * Transmits the array of recorded {@link SnaMessage}
	 * to the recipient
	 * 
	 * @param callbackId 
	 * 		the {@link SnaCallback}'s identifier from which
	 * 		the array of {@link SnaMessage}s come		
	 * @param messages
	 * 		the array of recorded {@link SnaMessage}
	 * 		to transmit
	 * @throws Exception
	 * 		If an error occurred while transmitting
	 * 		the messages array
	 */
	void callback(String callbackId, SnaMessage[] messages) 
			throws Exception;
	
}
