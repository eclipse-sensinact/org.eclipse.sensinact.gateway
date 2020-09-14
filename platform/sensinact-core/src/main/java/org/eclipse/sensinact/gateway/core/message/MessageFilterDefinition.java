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

import java.util.List;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;

/**
 * A FilterDefinition gathers the set of information allowing to create
 * an valid {@link MessageFilter}
 * 
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface MessageFilterDefinition {

	/**
	 * Returns the array of filtered message types - A message of a type not referenced 
	 * in the array argument will not be propagated
	 * 
	 * @return the array of filtered message types
	 */
	SnaMessage.Type[] handledTypes();

	/**
	 * Returns the List of {@link Constraint}s applying on the filtered messages - 
	 * A message that does not comply to the defined conditions will not be propagated
	 *  
	 * @return the List of {@link Constraint}s applying
	 */
	List<Constraint> conditions();

	/**
	 * Returns the path defining accepted source(s) of messages - A message
	 * coming from a different source will not be propagated
	 * 
	 * @return the accepted source(s) path
	 */
	String sender();

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
	boolean isPattern();

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
	boolean isComplement();
	
}
