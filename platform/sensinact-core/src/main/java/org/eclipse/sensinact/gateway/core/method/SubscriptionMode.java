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


import java.util.Vector;

import org.eclipse.sensinact.gateway.util.JSONUtils;


/**
 * handled subscription mode
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public enum SubscriptionMode
{
	NONE((byte)0x00),
    ON_CHANGE((byte)0x01), 
    SCHEDULED((byte)0x02),
    CONDITIONAL((byte)0x04),
    BUFFERIZED((byte)0x08),
    TIMED_OUT((byte)0x10);
    
    protected final byte mode;
    
    /**
     * Constructor
     * 
     * @param mode
     * 		byte identifier of the mode
     */
    SubscriptionMode(byte mode)
    {
    	this.mode = mode;
    }
    
    /**
     * Returns true if the SubscriptionMode passed
     * as parameter is present in the byte value 
     * representation of specified mode(s)
     * 
     * @param mode
     * 		byte value representation of subscription 
     * 		mode(s)
     * @param subscriptionMode
     * 		the subscription mode to check the presence 
     * 
     * @return
     */
    public static boolean contains(byte mode, 
    		SubscriptionMode subscriptionMode)
    {
    	return (mode & subscriptionMode.mode)
    			== subscriptionMode.mode;
    }
    
    /**
     * Builds and returns the byte value representation of
     * the {@link SubscriptionMode}s array passed as 
     * parameter
     * 
     * @param modes
     * 		the array of {@link SubscriptionMode}s to build
     * 		the byte value representation of
     * @return
     * 		the byte value representation of the specified 
     * 		{@link SubscriptionMode}s
     */
    public static byte valueOf(SubscriptionMode[] modes)
    {
    	byte mode = NONE.mode;
    	int index = 0;
    	int length = modes==null?0:modes.length;
    	for(;index < length; index++)
    	{
    		mode|=modes[index].mode;
    	}
    	return mode;
    }        
    
    /**
     * Converts the byte value passed as parameter into 
     * the array of {@link SubscriptionMode}s whose byte 
     * values composed the specified one
     * 
     * @param mode
     * 		the byte value representation to convert
     * @return
     * 		the {@link SubscriptionMode}s array based on 
     * 		the specified byte value representation
     */
    public static SubscriptionMode[] valueOf(byte mode)
    {
    	SubscriptionMode[] modes  = 
    			SubscriptionMode.values();
    	
    	Vector<SubscriptionMode> contained = 
    			new Vector<SubscriptionMode>();
    	
    	int index = 0;
    	int length = modes==null?0:modes.length;        	
    	
    	for(;index < length; index++)
    	{
    		if(SubscriptionMode.contains(mode, 
    				modes[index]))
    		{
    			contained.add(modes[index]);
    		}
    	}
    	return contained.toArray(
    			new SubscriptionMode[contained.size()]);
    }
	
	/** 
	 *  Returns the JSON formated String description of 
	 *  the array of {@link SubscriptionMode}s passed as 
	 *  parameter using its processed byte value 
	 *  representation
	 */
	public static String getJSON(SubscriptionMode[] modes) 
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append(JSONUtils.QUOTE);
		buffer.append("subscriptionMode");
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(SubscriptionMode.valueOf(modes));
		return buffer.toString();
	}
}
