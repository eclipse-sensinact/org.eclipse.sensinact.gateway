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

/**
 *	
 */
public interface MessageRegisterer 
{
	/**
	 * Registers the SnaMessage passed as parameter
	 * for a future transmission. 
	 * 
	 * @param message
	 * 		the {@link SnaMessage} to register
	 */
	void register(SnaMessage message);

//    /**
//     * Returns true if this callback handle unchanged value 
//     * notifications, meaning that only the timestamp has been
//     * updated. The default is false
//     * 
//     * 
//     * @return 
//     * 		<ul>
//     * 			<li>true if this callback handles unchanged value
//     * 			notifications</li>
//     * 			<li>false otherwise</li>
//     * 		</ul>
//     */
//    boolean handleUnchanged();
}
