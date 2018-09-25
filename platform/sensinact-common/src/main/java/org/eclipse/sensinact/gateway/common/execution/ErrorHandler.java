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

import java.util.Arrays;

import org.json.JSONArray;

/**
 * Error handler service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ErrorHandler {

    public static class Policy{

        public static final int CONTINUE = 0x000001;
        public static final int STOP = 0x000010;
        public static final int ROLLBACK = 0x000100;
        public static final int IGNORE = 0x001000;
        public static final int ALTERNATIVE = 0x010000;
        public static final int LOG = 0x100000;
        
        public static final int DEFAULT_POLICY = STOP | LOG;

	    /**
	     * Builds and returns the int value representation of
	     * the int policies array passed as parameter
	     *
	     * @param policies 
	     * 		the array of {@link ErrorHandlerPolicy}s to build
	     *      the byte value representation of
	     * @return 
	     * 		the byte value representation of the specified
	     * 		{@link ErrorHandlerPolicy}s
	     */
	    public static int valueOf(int[] policies) {
	        int policy = 0x0000;
	        int index = 0;
	        int length = policies == null ? 0 : policies.length;
	        for (; index < length; index++) {
	            policy |= policies[index];
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
        public static int[] valueOf(int policy) {        	
            int[] policies = new int[] {
            	CONTINUE,STOP,ROLLBACK,IGNORE,LOG
            };
            int[] effectivePolicies = new int[policies.length];
            int pos = 0;
            int index = 0;
            int length = policies == null ? 0 : policies.length;

            for (; index < length; index++) {
                if (Policy.contains(policy, policies[index])) {
                	effectivePolicies[pos++]=policies[index];
                }
            }
            if(pos==0){
            	return new int[0];
            }
            return Arrays.copyOfRange(effectivePolicies, 0, pos);
        }

	    /**
         * Returns true if the ErrorHandlerPolicy passed
         * as parameter is present in the byte value
         * representation of specified policy(s)
         *
         * @param policy             
         * 		the global policy integer value applying
         * @param errorHandlerPolicy 
         * 		the policy integer value to identify
         * @return
         */
        public static boolean contains(int policy, int errorHandlerPolicy) {
            return (policy & errorHandlerPolicy) == errorHandlerPolicy;
        }
    }
	
    /**
     * Registers an {@link Exception} to this ErrorHandler and 
     * returns an integer identifying the continuation to be applied
     * on the current execution according to the defined error policy
     *
     * @param e 
     * 		the {@link Exception} to be registered
     * @return 
     * 		the continuation integer identifier
     */
    int handle(Exception e);

    /**
     * Returns this ErrorHandler's handling policy
     * byte value representation
     *
     * @return handling policy byte value representation
     * of this handler
     */
    int getPolicy();

    /**
     * Returns the traces of registered exceptions
     * as a JSON formated array
     *
     * @return the JSONArray of registered exceptions traces
     */
    JSONArray getStackTrace();

    /**
     * Returns the number of exceptions registered by this
     * ErrorHandler
     * 
     * @return 
     * 		the number of registered exceptions
     */
    int getExceptions();
    
    /**
     * Sets the alternative execution to be executed when an
     * error occurred and if it has been defined by the policy 
     * of this ErrorHandler
     *
     * @param alternative 
     * 		the alternative execution
     */
     void setAlternative(Execution alternative);

    /**
     * Sets the array of parameters to be used as arguments of the 
     * alternative execution
     *
     * @param parameters 
     * 		the array of parameters of the alternative execution
     */
     void setAlternativeParameters(Object[] parameters);

     /**
      * Returns the result Object of the alternative execution
      *
      * @return 
      * 	the result Object of the alternative execution
      */
      Object getAlternativeResult();
     
}
