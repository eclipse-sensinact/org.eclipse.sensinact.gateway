/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.eclipse.sensinact.gateway.core.message.AgentRelay;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;



/**
 * Filter annotation allows to define the filtering process to be used 
 * by an {@link Agent} based on an {@link AgentRelay} service implementation
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Filter {	
	
	/**
	 * Returns the path defining accepted source(s) of messages - A message
	 * coming from a different source will not be propagated
	 * 
	 * @return the accepted source(s) path
	 */
	String sender() default "(/[^/]+)+";
	
	/**
	 * Returns true if the sender attribute is an regular expression - 
	 * returns false otherwise
	 * 
	 * @return
	 * <ul>
	 * 		<li>true if the sender attribute is an regular expression</li>
	 * 		<li>false otherwise</li>
	 * </ul>
	 */
	boolean isPattern() default true;
	
	/**
	 * Returns true if the filtering process refers to the logical complement
	 * of both sender and conditions arguments for message validation - returns
	 * false otherwise
	 *  
	 * @return
	 * <ul>
	 * 		<li>true if the messages are filtered using the logical complement</li>
	 * 		<li>false otherwise</li>
	 * </ul> 
	 */
	boolean isComplement() default false;	
	
	/**
	 * Returns the array of filtered message types - A message of a different type
	 * will not be propagated
	 * 
	 * @return the array of filtered message types
	 */
	SnaMessage.Type[] handled() default {};
	
	/**
	 * Returns the array of the JSON formated string representations of 
	 * the constraints applying on the filtered messages - A message that does
	 * not comply to the defined conditions will not be propagated
	 * 
	 * @see org.eclipse.sensinact.gateway.common.constraint.Constraint;
	 * 
	 * @return the array of JSON formated string constraints applying
	 */
	String[] conditions() default {};
}
