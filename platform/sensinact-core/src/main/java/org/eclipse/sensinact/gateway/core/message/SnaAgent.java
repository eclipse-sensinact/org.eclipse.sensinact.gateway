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
 * Recipient of messages of the system relative to SensiNactResourceModels
 * lifecycle or data value updates
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaAgent extends MessageRegisterer
{		
	/**
	 * Stops this SnaAgent
	 */
    void stop();
    
    /**
     * Returns the String public key of this SnaAgent
     * used to defined access level to transmitted messages
     * 
     * @return this SnaAgent's public key
     */
    String getPublicKey();
}
