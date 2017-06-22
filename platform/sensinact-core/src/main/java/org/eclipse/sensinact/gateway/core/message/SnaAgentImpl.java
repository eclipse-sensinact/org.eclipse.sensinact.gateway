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

import org.eclipse.sensinact.gateway.util.stack.StackEngineHandler;
import org.json.JSONArray;
import org.json.JSONException;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.stack.AbstractStackEngineHandler;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SnaAgentImpl extends AbstractStackEngineHandler<SnaMessage<?>> 
implements SnaAgent
{    	

	//********************************************************************//
	//						STATIC DECLARATIONS  						  //
	//********************************************************************//
	
	public static final String SNAFILTER_AGENT_SUFFIX_PROPERTY = "org.eclipse.sensinact.gateway.filter.suffix";
	public static final String SNAFILTER_AGENT_TYPES_PROPERTY = "org.eclipse.sensinact.gateway.filter.types";
	public static final String SNAFILTER_AGENT_SENDER_PROPERTY = "org.eclipse.sensinact.gateway.filter.sender";
	public static final String SNAFILTER_AGENT_PATTERN_PROPERTY = "org.eclipse.sensinact.gateway.filter.pattern";
	public static final String SNAFILTER_AGENT_COMPLEMENT_PROPERTY = "org.eclipse.sensinact.gateway.filter.complement";
	public static final String SNAFILTER_AGENT_CONDITIONS_PROPERTY = "org.eclipse.sensinact.gateway.filter.conditions";

	public static final String COMMA = ",";
	public static final String DOT = ".";
	
	/**
     * Creates an {@link SnaAgent} with the callback 
     * is passed as parameter
     * 
     * @param mediator
     * @param callback
     *      the {@link AbstractSnaAgentCallback} that will 
     *      be called by the SnaAgent to be created
     * @param filter
     * @return
     */
    public static SnaAgentImpl createAgent(
    	Mediator mediator, SnaAgentCallback callback, 
    	SnaFilter filter, String agentKey)
    {
    	String suffix = (String) mediator.getProperty(
				SNAFILTER_AGENT_SUFFIX_PROPERTY);
		
		if(filter == null && suffix != null)
		{				
			boolean isPattern = false;
			String sender = SnaAgentImpl.getSender(
					mediator, suffix);
			
			if(sender == null)
			{
				sender = ".*";
				isPattern = true;
				
			} else
			{
				isPattern = SnaAgentImpl.isPattern(
					mediator, suffix);
			}
			boolean isComplement = SnaAgentImpl.isComplement(
					mediator, suffix);
			JSONArray conditions = SnaAgentImpl.getConditions(
					mediator, suffix);
			SnaMessage.Type[] types = SnaAgentImpl.getTypes(
					mediator, suffix);
			
			filter = new SnaFilter(mediator, sender,
					isPattern, isComplement, conditions);
			int index = 0; 
			int length = types.length;
			
			for(;index < length;index++)
			{
				filter.addHandledType(types[index]);
			}
		}
		SnaAgentImpl agent = new SnaAgentImpl(mediator, 
			callback, filter, agentKey);
		
		return agent;
    }
    
	/**
	 * 
	 * @param mediator
	 * @param suffix
	 * @return
	 */
	private static JSONArray getConditions(
			Mediator mediator, String suffix)
	{
		JSONArray conditions = null;
		
		String conditionsStr = (String) mediator.getProperty(
			buildProperty(SNAFILTER_AGENT_CONDITIONS_PROPERTY,
				suffix));
		
		if(conditionsStr == null)
		{
			conditions = new JSONArray();
			
		} else
		{
			try
			{
				conditions = new JSONArray(
						conditionsStr);
				
			} catch(JSONException e)
			{
				conditions = new JSONArray();
			}
		}
		return conditions;
	}
	
	/**
	 * @param mediator
	 * @param suffix
	 * @return
	 */
	private static SnaMessage.Type[] getTypes(
			Mediator mediator, String suffix)
	{
		SnaMessage.Type[] messageTypes = null;
		
		String typesStr = (String) mediator.getProperty(
				buildProperty(SNAFILTER_AGENT_TYPES_PROPERTY,
						suffix));
		
		if(typesStr == null)
		{
			messageTypes = SnaMessage.Type.values();
			
		} else
		{
			String[] typesArray = typesStr.split(COMMA);
			messageTypes = new SnaMessage.Type[typesArray.length];					
			int index = 0;
			int length = typesArray.length;					
			try
			{
				for(;index< length;index++)
				{
					messageTypes[index] = SnaMessage.Type.valueOf(
							typesArray[index]);
				}						
			} catch(IllegalArgumentException e)
			{
				messageTypes = SnaMessage.Type.values();
			}
		}
		return messageTypes;
	}
	
	/**
	 * @param mediator
	 * @param suffix
	 * @return
	 */
	private static String getSender(
			Mediator mediator, 
			String suffix)
	{
		return (String) mediator.getProperty(
				buildProperty(SNAFILTER_AGENT_SENDER_PROPERTY,
						suffix));
	}

	/**
	 * @param mediator
	 * @param suffix
	 * @return
	 */
	private static boolean isPattern(
			Mediator mediator, 
			String suffix)
	{
		boolean isPattern = false;
		String patternStr = (String) mediator.getProperty(
				buildProperty(SNAFILTER_AGENT_PATTERN_PROPERTY,
						suffix));
		if(patternStr != null)
		{
			isPattern = Boolean.parseBoolean(patternStr);
		}
		return isPattern;
	}

	/**
	 * @param mediator
	 * @param suffix
	 * @return
	 */
	private static boolean isComplement(
			Mediator mediator, String suffix)
	{
		boolean isComplement = false;
		String complementStr = (String) mediator.getProperty(
				buildProperty(SNAFILTER_AGENT_COMPLEMENT_PROPERTY,
						suffix));
		if(complementStr != null)
		{
			isComplement = Boolean.parseBoolean(complementStr);
		}
		return isComplement;
	}
	
	/**
	 * @param property
	 * @param suffix
	 * @return
	 */
	private static String buildProperty(String property, String suffix)
	{
		return new StringBuilder().append(property).append(DOT
				).append(suffix).toString();
	}

    
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	/**
     * the {@link AbstractSnaAgentCallback} type 
     */
	private final SnaAgentCallback callback;
	
	/**
	 * the {@link SnaFilter} used to validate the 
	 * received {@link SnaMessage} before transmitting
	 * them to the dedicated {@link AbstractSnaAgentCallback}
	 */
	private SnaFilter filter;
	
	/**
	 * the String public key of this {@link SnaAgent}
	 */
	private final String publicKey;
	
	/**
	 * the {@link Mediator} allowing to interact
	 * with the OSGi host environment
	 */
	private Mediator mediator;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param callback
	 */
	public SnaAgentImpl(Mediator mediator, 
		SnaAgentCallback callback, String publicKey)
	{
		this(mediator, callback, null, publicKey);		
	}
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param callback
	 * @param filter
	 */
	public SnaAgentImpl(Mediator mediator, 
		SnaAgentCallback callback, SnaFilter filter, 
		String publicKey)
	{
		super();
		this.mediator = mediator;
		this.publicKey = publicKey;
		this.callback = callback;	
		this.filter = filter;
		
		if(this.filter == null)
		{
			this.filter = new SnaFilter(mediator, ".*", true, false);
			this.filter.addHandledType(SnaMessage.Type.values());
		}
	}

    /**
     * @inheritDoc
     *
	 * @see MessageRegisterer#
	 * register(SnaMessage)
	 */
    @Override
    public synchronized void register(SnaMessage message)
    {
    	if(this.filter != null && !filter.matches(message))
    	{
    		return;
    	}
    	super.eventEngine.push(message);
    }

    /**
     * @inheritDoc
     *
     * @see SnaAgent#getPublicKey()
     */
    public String getPublicKey()
    {
    	return this.publicKey;
    }
    
    /**
     * @inheritDoc
     *
	 * @see StackEngineHandler#doHandle(java.lang.Object)
	 */
    @Override
    public void doHandle(SnaMessage message)
    {
    	try
        {
	        this.callback.register(message);
        }
        catch (Exception e)
        {
        	this.mediator.error(e);
        }
    }   

    /**
     * @inheritDoc
     *
     * @see SnaAgent#stop()
     */
    @Override
    public void stop()
    {
    	super.close();
    	try
    	{
    		this.callback.stop();
    		
    	} catch(Exception e)
    	{
        	this.mediator.error(e);
    	}
    }

//	/** 
//	 * @inheritDoc
//	 * 
//	 * @see MessageRegisterer#handleUnchanged()
//	 */
//	@Override
//	public boolean handleUnchanged()
//	{
//		return this.callback.handleUnchanged();
//	}
}
