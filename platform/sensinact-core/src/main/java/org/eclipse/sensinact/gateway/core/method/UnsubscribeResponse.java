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
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;


/**
 * Extended {@link AbstractSnaMessage} returned by an
 * {@link UnsubscribeMethod} invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnsubscribeResponse extends AccessMethodResponse
{
	/**
	 * Constructor 
	 * 
	 * @param status
	 * 		the associated {@link Status}
	 */
    protected UnsubscribeResponse(Mediator mediator, 
    		String uri, Status status)
    {
	    this(mediator, uri, status, Status.SUCCESS.equals(status)
	    	? SnaErrorfulMessage.NO_ERROR:SnaErrorfulMessage.UNKNOWN_ERROR_CODE);
    }	
    
    /**
	 * Constructor 
	 * 
	 * @param status
	 * 		the associated {@link Status}
	 * @param code
	 * 		the associated status code 
	 */
    protected UnsubscribeResponse(Mediator mediator, 
    		String uri, Status status, int code)
    {
    	super(mediator, uri, AccessMethodResponse.Response.UNSUBSCRIBE_RESPONSE,
    		status, code);
    }
}
