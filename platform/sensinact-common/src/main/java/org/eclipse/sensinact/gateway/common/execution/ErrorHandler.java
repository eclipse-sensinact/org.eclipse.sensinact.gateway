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
package org.eclipse.sensinact.gateway.common.execution;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Error handler service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ErrorHandler
{
	/**
	 * Possible error handling 
	 * policy
	 */
	enum ErrorHandlerPolicy
	{		
		NONE((byte)0x00),
		BREAK((byte)0x01),
		CONTINUE((byte)0x02),
		ROLLBACK((byte)0x04), 
		REMOVE((byte)0x08),
		ALTERNATIVE((byte)0x10);
		
		protected final byte policy;
	    
	    /**
	     * Constructor
	     * 
	     * @param policy
	     * 		byte identifier of the policy
	     */
		ErrorHandlerPolicy(byte policy)
	    {
	    	this.policy = policy;
	    }
	    
	    /**
	     * Returns true if the ErrorHandlerPolicy passed
	     * as parameter is present in the byte value 
	     * representation of specified policy(s)
	     * 
	     * @param policy
	     * 		byte value representation of subscription 
	     * 		policy(s)
	     * @param errorHandlerPolicy
	     * 		the subscription policy to check the presence 
	     * 
	     * @return
	     */
	    public static boolean contains(byte policy, 
	    		ErrorHandlerPolicy errorHandlerPolicy)
	    {
	    	return (policy & errorHandlerPolicy.policy)
	    			== errorHandlerPolicy.policy;
	    }
	    
	    /**
	     * Builds and returns the byte value representation of
	     * the {@link ErrorHandlerPolicy}s array passed as 
	     * parameter
	     * 
	     * @param policies
	     * 		the array of {@link ErrorHandlerPolicy}s to build
	     * 		the byte value representation of
	     * @return
	     * 		the byte value representation of the specified 
	     * 		{@link ErrorHandlerPolicy}s
	     */
	    public static byte valueOf(ErrorHandlerPolicy[] policies)
	    {
	    	byte policy = NONE.policy;
	    	int index = 0;
	    	int length = policies==null?0:policies.length;
	    	for(;index < length; index++)
	    	{
	    		policy|=policies[index].policy;
	    	}
	    	return policy;
	    }        
	    
	    /**
	     * Converts the byte value passed as parameter into 
	     * the array of {@link ErrorHandlerPolicy}s whose byte 
	     * values composed the specified one
	     * 
	     * @param policy
	     * 		the byte value representation to convert
	     * @return
	     * 		the {@link ErrorHandlerPolicy}s array based on 
	     * 		the specified byte value representation
	     */
	    public static ErrorHandlerPolicy[] valueOf(byte policy)
	    {
	    	ErrorHandlerPolicy[] policies  = 
	    			ErrorHandlerPolicy.values();
	    	
	    	List<ErrorHandlerPolicy> contained = 
	    			new ArrayList<ErrorHandlerPolicy>();
	    	
	    	int index = 0;
	    	int length = policies==null?0:policies.length;        	
	    	
	    	for(;index < length; index++)
	    	{
	    		if(ErrorHandlerPolicy.contains(policy, 
	    				policies[index]))
	    		{
	    			contained.add(policies[index]);
	    		}
	    	}
	    	return contained.toArray(
	    			new ErrorHandlerPolicy[contained.size()]);
	    }
		
		/** 
		 *  Returns the JSON formated String description of 
		 *  the array of {@link ErrorHandlerPolicy}s passed as 
		 *  parameter using its processed byte value 
		 *  representation
		 */
		public static String getJSON(ErrorHandlerPolicy[] policies) 
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append(JSONUtils.QUOTE);
			buffer.append("errorHandlerPolicy");
			buffer.append(JSONUtils.QUOTE);
			buffer.append(JSONUtils.COLON);
			buffer.append(ErrorHandlerPolicy.valueOf(policies));
			return buffer.toString();
		}
	}
	
	/**
	 * Registers an {@link Exception} to this
	 * error handler
	 * 
	 * @param e
	 * 		the {@link Exception} to register
	 */
	void register(Exception e);
	
	/**
	 * Returns true if exceptions has been registered
	 * into this SnaErrorHandler; otherwise returns 
	 * false
	 * 
	 * @return
	 * 		<ul>
	 * 			<li>true if this handler contains registered
	 * 				exceptions</li>
	 * 			<li>false otherwise</li>
	 * 		</ul>
	 */
	boolean hasError();
	
	/**
	 * Returns this SnaErrorHandler's handling policy 
	 * byte value representation
	 * 
	 * @return
	 * 		handling policy byte value representation
	 * 		of this handler
	 */
	byte getPolicy();
	
	/**
	 * Returns the traces of registered exceptions
	 * as a JSON formated array
	 * 
	 * @return
	 * 		the JSONArray of registered exceptions'
	 * 		traces
	 */
	JSONArray getStackTrace();
		
	/**
	 * Returns the alternative execution as a list
	 * of {@link Executors}
	 *  
	 * @return
	 * 		the alternative execution as a list
	 * 		of {@link Executors}
	 */
	<E extends Executable<?,?>> List<E> getAlternative();
}
