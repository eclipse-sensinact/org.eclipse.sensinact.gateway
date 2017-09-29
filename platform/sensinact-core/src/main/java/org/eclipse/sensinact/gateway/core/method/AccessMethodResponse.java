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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaMessageSubType;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.core.message.AbstractSnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;

/**
 * Extended {@link SnaMessage} dedicated to the responses 
 * to the {@link AccessMethod}s invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AccessMethodResponse 
extends AbstractSnaErrorfulMessage<AccessMethodResponse.Response>
implements SnaResponseMessage<AccessMethodResponse.Response>
{
	public static final int SUCCESS_CODE = SnaErrorfulMessage.NO_ERROR;
	
	public enum Response implements SnaMessageSubType, KeysCollection
	{
		DESCRIBE_RESPONSE,
		GET_RESPONSE,
		SET_RESPONSE,
		ACT_RESPONSE,
		SUBSCRIBE_RESPONSE,
		UNSUBSCRIBE_RESPONSE,
		UNKNOWN_METHOD_RESPONSE;

		final Set<TypedKey<?>> keys;
		
		Response()
		{
			List<TypedKey<?>> list = Arrays.asList(new SnaMessage.KeysBuilder(
						AccessMethodResponse.class).keys());
			
			Set<TypedKey<?>> tmpKeys = new HashSet<TypedKey<?>>();
			tmpKeys.addAll(list);
			keys = Collections.unmodifiableSet(tmpKeys);
		}

		/** 
		 * @inheritDoc
		 * 
		 * @see SnaMessageSubType#getSnaMessageType()
		 */
		@Override
		public SnaMessage.Type getSnaMessageType()
		{
			return SnaResponseMessage.TYPE;
		}

		/** 
		 * @inheritDoc
		 * 
		 * @see KeysCollection#keys()
		 */
		@Override
		public Set<TypedKey<?>> keys()
		{
			return this.keys;
		}

		/** 
		 * @inheritDoc
		 * 
		 * @see KeysCollection#key(java.lang.String)
		 */
		@Override
		public TypedKey<?> key(String key)
		{
			TypedKey<?> typedKey = null;
			
			Iterator<TypedKey<?>> iterator = this.keys.iterator();
			while(iterator.hasNext())
			{
				typedKey = iterator.next();
				if(typedKey.equals(key))
				{
					break;
				}
				typedKey = null;
			}
			return typedKey;
		}
	}
	
	/**
	 * SnaObjectMessage possible
	 * status
	 */
	public enum Status
	{
		ERROR,
		SUCCESS;
	}


	/**
	 * Error SnaMessages factory
	 * 
	 * @param mediator
	 * @param uri
	 * @param method
	 * @param statusCode
	 * @param message
	 * @param throwable
	 * @return
	 */
	public static final AccessMethodResponse error(Mediator mediator, 
			String uri, AccessMethod.Type method, int statusCode, 
			String message, Throwable throwable)
	{
		return AccessMethodResponse.error(mediator, uri, method.name(), 
			statusCode, message, throwable);
	}

	/**
	 * Error SnaMessages factory
	 * 
	 * @param mediator
	 * @param uri
	 * @param method
	 * @param statusCode
	 * @param message
	 * @param throwable
	 * @return
	 */
	public static final AccessMethodResponse error(Mediator mediator, 
			String uri, String method, int statusCode, 
			String message, Throwable throwable)
	{
		int code = statusCode==AccessMethodResponse.SUCCESS_CODE
				?SnaErrorfulMessage.UNKNOWN_ERROR_CODE:statusCode;	

		AccessMethodResponse snaResponse = null;
		switch(method)
		{
			case "ACT":
				snaResponse = new ActResponse(mediator, uri,
						AccessMethodResponse.Status.ERROR, code);
				break;
			case "DESCRIBE":
				snaResponse = new DescribeResponse(mediator, uri,
						AccessMethodResponse.Status.ERROR, code);
				break;
			case "GET":
				snaResponse = new GetResponse(mediator, uri,
						AccessMethodResponse.Status.ERROR, code);
				break;
			case "SET":
				snaResponse = new SetResponse(mediator, uri,
						AccessMethodResponse.Status.ERROR, code);
				break;
			case "SUBSCRIBE":
				snaResponse = new SubscribeResponse(mediator, uri,
						AccessMethodResponse.Status.ERROR, code);
				break;
			case "UNSUBSCRIBE":
				snaResponse = new UnsubscribeResponse(mediator, uri,
						AccessMethodResponse.Status.ERROR, code);
				break;
			default:
				snaResponse = new UnknownAccessMethodResponse(
						mediator,uri);
				break;
		}
		if(snaResponse!=null && message != null)
		{
			snaResponse.setErrors(message, throwable);
		}
		return snaResponse;
	}
	
	/**
	 * this SnaMessage status
	 */
	private final Status status;
	
	/**
	 * @param uri
	 * @param type
	 * @param status
	 */
    protected AccessMethodResponse(Mediator mediator, 
    		String uri, Response type, Status status)
    {
    	this(mediator, uri,type, status,(status == Status.SUCCESS)
		?AccessMethodResponse.SUCCESS_CODE: UNKNOWN_ERROR_CODE);
    }

    /**
	 * @param uri
	 * @param type
	 * @param status
	 */
    protected AccessMethodResponse(Mediator mediator, 
    		String uri, Response type, Status status, int statusCode)
    {
	    super(mediator, uri, type);
	    this.status = status;
		super.putValue(SnaConstants.STATUS_CODE_KEY, statusCode);
    }
    
    /**
     * @param jsonObject
     */
    public void setResponse(JSONObject jsonObject)
    {
    	if(JSONObject.NULL.equals(jsonObject))
    	{
    		return;
    	}
    	super.putValue(SnaConstants.RESPONSE_KEY, jsonObject);
    }
    
    /**
     * @return
     */
    public JSONObject getResponse()
    {
    	JSONObject jsonObject = super.<JSONObject>get(
    			SnaConstants.RESPONSE_KEY);
    	return jsonObject;
    }    

    /**
     * @param key
     * @return
     */
    public Object getResponse(String key)
    {
    	Object value = null;
    	JSONObject jsonObject = getResponse();
    	
    	if(jsonObject != null)
    	{
    		value = jsonObject.opt(key);
    	}
    	return value;
    }

    /**
     * @param type
     * @param key
     * @return
     */
    public <T> T getResponse(Class<T> type , String key)
    {   	
    	return CastUtils.cast(super.mediator.getClassLoader(), 
    			type, getResponse(key));
    }

	/**
	 * Returns the state of this message 
	 *  
	 * @return
	 */
	public Status getStatus()
	{
		return this.status;
	}
	

	/**
	 * Returns the code of this SnaMessage's status
	 * 
	 * @return
	 * 		the code of this SnaMessage's {@link Status}
	 */
	public int getStatusCode()
	{
		return super.<Integer>get(SnaConstants.STATUS_CODE_KEY);
	}
	
	
	/**
	 * Defines this SnaMessage's status code
	 * 
	 * @param statusCode
	 * 		the status code to set
	 * @return
	 * 		the status code of this SnaMessage's status
	 */
	public int setStatusCode(int statusCode)
	{
		if(this.status == Status.ERROR)	
		{
			super.putValue(SnaConstants.STATUS_CODE_KEY, statusCode);
		}
		return super.<Integer>get(SnaConstants.STATUS_CODE_KEY);
	}
}
