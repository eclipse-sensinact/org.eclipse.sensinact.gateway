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


import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;

/**
 * Abstract implementation of an {@link AbstractSnaErrorfulMessage}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaErrorfulMessage<S extends Enum<S> 
& KeysCollection & SnaMessageSubType> extends AbstractSnaMessage<S> 
implements SnaErrorfulMessage<S>
{	
	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	protected AbstractSnaErrorfulMessage(Mediator mediator,	
			String uri, S type)
	{
		super(mediator, uri, type);		
	}

	/**
	 * @InheritedDoc
	 *
	 * @see SnaErrorfulMessage#setErrors(org.json.JSONArray)
	 */
	@Override
	public void setErrors(JSONArray errorsArray)
	{
		int length =0;
		
		if(errorsArray == null 
				||(length=errorsArray.length())==0)
		{
			return;
		}
		JSONArray errors = getErrors();
		if(errors == null)
		{
			errors = new JSONArray();
		}
		int index = 0;
		for(;index < length; index++)
		{
			errors.put(errorsArray.get(index));
		}
		super.putValue(SnaConstants.ERRORS_KEY, errors);
	}
	
    /**
     *
     * @param message
     * 		the thrown error message
     * @param exception
     * 		the thrown exception
     */
    protected void setErrors(String message, Throwable exception)
    {
    	JSONArray exceptionsArray = new JSONArray();
				
		JSONObject exceptionObject = new JSONObject();				
		exceptionObject.put("message", message==null?
				exception.getMessage():message);
		
		StringBuilder buffer = new StringBuilder();	
		if(exception !=null)
		{
			StackTraceElement[] trace = exception.getStackTrace();
			
			int index = 0;
			int length = trace.length;
			
			for(;index < length; index++)
			{
				buffer.append(trace[index].toString());
				buffer.append("\n");
			}
		}
		exceptionObject.put("trace", buffer.toString());
		exceptionsArray.put(exceptionObject);

		this.setErrors(exceptionsArray);
    }

	/** 
	 * @inheritDoc
	 * 
	 * @see SnaErrorfulMessage#
	 * setErrors(java.lang.Exception)
	 */
	@Override
	public void setErrors(Exception exception)
	{
		this.setErrors(null, exception);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see SnaErrorfulMessage#getErrors()
	 */
	@Override
	public JSONArray getErrors() 
	{
		return super.<JSONArray>get(SnaConstants.ERRORS_KEY);
	}
}
