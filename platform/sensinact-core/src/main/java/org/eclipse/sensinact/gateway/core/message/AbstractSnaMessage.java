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


import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedProperties;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessage.Lifecycle;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessage.Update;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Response;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SetResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.SubscribeResponse;
import org.eclipse.sensinact.gateway.core.method.legacy.UnsubscribeResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;

/**
 * Abstract implementation of an {@link AbstractSnaMessage}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractSnaMessage<S extends Enum<S> 
& KeysCollection & SnaMessageSubType> extends TypedProperties<S> 
implements SnaMessage<S>
{			
	public static SnaMessage<?> fromJSON(
			final Mediator mediator, 
			String json)
	{
		final JSONObject jsonMessage = new JSONObject(json); 
		final String typeStr = (String) jsonMessage.remove("type");
		final String uri = (String) jsonMessage.remove("uri");
		if(typeStr == null)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		Executable<Void,SnaMessage<?>>[] converters= new Executable[]{
			new Executable<Void,SnaLifecycleMessage>()
			{
				@Override
				public SnaLifecycleMessage execute(Void v) 
						throws Exception
				{
					Lifecycle l= Lifecycle.valueOf(typeStr);
					return new SnaLifecycleMessageImpl(mediator, uri, l);
				}
			},
			new Executable<Void,SnaUpdateMessage>()
			{
				@Override
				public SnaUpdateMessage execute(Void v) 
						throws Exception
				{
					Update u = Update.valueOf(typeStr);
					return new SnaUpdateMessageImpl(mediator, uri, u);
				}
			},
			new Executable<Void,SnaErrorMessage>()
			{
				@Override
				public SnaErrorMessage execute(Void v) 
						throws Exception
				{
					SnaErrorMessage.Error e = SnaErrorMessage.Error.valueOf(typeStr);
					return new SnaErrorMessageImpl(mediator, uri, e); 
				}
			},
			new Executable<Void,AccessMethodResponse>()
			{
				@Override
				public AccessMethodResponse execute(Void v) 
						throws Exception
				{
					Response r = Response.valueOf(typeStr);
					Integer icode = (Integer) jsonMessage.remove("statusCode");
					int code = icode == null?520:icode.intValue();
					Status status = code!=200?Status.ERROR:Status.SUCCESS;

					switch(r)
					{
						case ACT_RESPONSE:
							return new ActResponse(mediator, uri, status, code);
						case DESCRIBE_RESPONSE:
							return new DescribeResponse(mediator, uri,status,  code);
						case GET_RESPONSE:
							return new GetResponse(mediator, uri, status, code);
						case SET_RESPONSE:
							return new SetResponse(mediator, uri, status, code);
						case SUBSCRIBE_RESPONSE:
							return new SubscribeResponse(mediator, uri,status, code);
						case UNSUBSCRIBE_RESPONSE:
							return new UnsubscribeResponse(mediator, uri,status, code);
						default:
							break;
					}
					return null;
				}
			}
		};
		SnaMessage<?> message = null;
		for(Executable<Void,SnaMessage<?>> converter:converters)
		{
			try
			{
				message = converter.execute(null);
				break;
				
			} catch(Exception e)
			{
				continue;
			}
		}
		if(message != null)
		{
			JSONArray names = jsonMessage.names();
			int index = 0;
			int length = names == null?0:names.length();
			for(;index < length; index++)
			{
				String name = names.getString(index);
				((TypedProperties<?>)message).put(
						name, jsonMessage.get(name));
			}
		}
		return message;
	}
	
	/**
	 * Constructor
	 * 
	 * @param uri
	 * @param type
	 */
	protected AbstractSnaMessage(Mediator mediator, String uri, S type)
	{
		super(mediator, type);
		super.putValue(SnaConstants.URI_KEY, uri);
		
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see PathElement#getPath()
	 */
	public String getPath()
	{
		return super.<String>get(SnaConstants.URI_KEY);
	}

	/**
	 * Returns the {@link SnaMessage.Type} to which
	 * this extended {@link SnaMessage}'s type belongs to
	 */
	public SnaMessage.Type getSnaMessageType()
	{
		S type = super.getType();
		return type.getSnaMessageType();
	}
	
}
