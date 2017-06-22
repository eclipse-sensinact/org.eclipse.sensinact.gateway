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

import org.eclipse.sensinact.gateway.generic.Task;

/**
 * Sub Packet data structure holding the 
 * set of data allowing to update an 
 * element of the resource model
 */
public interface SubPacket
{
	/**
	 * The {@link Task.CommandType} targeting the
	 * element whose path is defined by the 
	 * service provider identifier, the service 
	 * identifier and the resource identifier 
	 * successively
	 * 
	 * @return this SubPacket's {@link Task.CommandType}
	 */
	Task.CommandType getCommand();

	/**
	 * Returns true if this SubPacket is the 'Hello' 
	 * message of the {@link ServiceProvider} whose 
	 * identifier is attach
	 * 
	 * @return 
	 * 	<ul>
	 * 		<li>true if thise SubPacket is an 'Hello' message</li>
	 * 		<li>false otherwise </li>
	 * 	</ul>
	 */
	boolean isHelloMessage();

	/**
	 * Returns true if this SubPacket is the 'GoodBye' 
	 * message of the {@link ServiceProvider} whose 
	 * identifier is attach
	 * 
	 * @return 
	 * 	<ul>
	 * 		<li>true if this SubPacket is an 'GoodBye' message</li>
	 * 		<li>false otherwise </li>
	 * 	</ul>
	 */
	boolean isGoodbyeMessage();

	/**
	 * Returns the String identifier of the profile
	 * of the targeted {@link ServiceProvider}
	 * 
	 * @return the profile of the targeted {@link 
	 * ServiceProvider}'s identifier
	 */
	String getProfileId();
	
	/**
	 * Returns the String identifier of the
	 * targeted {@link ServiceProvider}
	 * 
	 * @return the targeted {@link ServiceProvider}'s
	 * identifier
	 */
	String getServiceProviderId();
	
	/**
	 * Returns the String identifier of the
	 * targeted {@link Service}
	 * 
	 * @return the targeted {@link Service}'s
	 * identifier
	 */
	String getServiceId();
	
	/**
	 * Returns the String identifier of the
	 * targeted {@link Resource}
	 * 
	 * @return the targeted {@link Resource}'s
	 * identifier
	 */
	String getResourceId();

	/**
	 * Returns the String identifier of the
	 * targeted {@link Attribute}
	 * 
	 * @return the targeted {@link Attribute}'s
	 * identifier
	 */
	String getAttributeId();

	/**
	 * Returns the String identifier of the
	 * targeted {@link Metadata}
	 * 
	 * @return the targeted {@link Metadata}'s
	 * identifier
	 */
	String getMetadataId();

	/**
	 * Returns the Object data value held by this
	 * SubPacket
	 * 
	 * @return this SubPacket's Object data value
	 */
	Object getData();

	/**
	 * Returns the long time-stamp of this
	 * SubPacket
	 * 
	 * @return this SubPacket's long time-stamp
	 */
	long getTimestamp();
}
