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

import java.util.Set;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.common.primitive.Typable;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;


/**
 *  A method provided by an {@link ModelElementProxy}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethod extends Nameable, Describable, 
PathElement, Typable<AccessMethod.Type>
{       
	/**
	 * Type of an AccessMethod
	 */
	public enum Type
	{
		GET(AccessMethodResponse.Response.GET_RESPONSE),
		SET(AccessMethodResponse.Response.SET_RESPONSE),
		ACT(AccessMethodResponse.Response.ACT_RESPONSE),
		SUBSCRIBE(AccessMethodResponse.Response.SUBSCRIBE_RESPONSE),
		UNSUBSCRIBE(AccessMethodResponse.Response.UNSUBSCRIBE_RESPONSE),
		DESCRIBE(AccessMethodResponse.Response.DESCRIBE_RESPONSE);
		
		/**
		 * the extended {@link SnaMessage} type returned
		 * 		by an AccessMethod of this type
		 */
		private final AccessMethodResponse.Response responseType; 
		
		/**
		 * Constructor
		 * 
		 * @param responseType
		 * 		the extended {@link SnaMessage} type returned
		 * 		by an AccessMethod of this type
		 */
		Type(AccessMethodResponse.Response responseType)
		{
			this.responseType = responseType;
		}
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.core.model.method.AccessMethodType#getMessageType()
		 */
        public AccessMethodResponse.Response getReturnedType()
        {
	        return this.responseType;
        }
	}

	Object EMPTY = new Object();
	
    /**
     * Returns the set of {@link Signature}s of this 
     * AccessMethod
     * 
     * @return
     * 		this AccessMethod's {@link Signature}s
     */
    Set<Signature> getSignatures();
     
	/**
	 * Returns the number of {@link Signature}s of
	 * this AccessMethod
	 * 
	 * @return
	 * 		the number of {@link Signature}s of
	 * 		this AccessMethod
	 */
    int size();
    
   	/**
   	 * Returns {@link ErrorHandler} used to handle
   	 * an error if it occurs
   	 * 
   	 * @return
   	 * 		the {@link ErrorHandler} handling errors
   	 */
    ErrorHandler getErrorHandler();
    
    /**
     * Invokes this method by using the objects array argument 
     * to parameterize the call
     * 
     * @param parameters
     * 		the objects array parameterizing the call
     * @return
     * 		the {@link AccessMethodResponse} result of the
     * 		invocation
     */
    AccessMethodResponse invoke(Object[] parameters);
    
    
}
