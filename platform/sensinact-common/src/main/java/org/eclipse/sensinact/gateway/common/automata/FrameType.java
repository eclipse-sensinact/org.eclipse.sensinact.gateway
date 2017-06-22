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

package org.eclipse.sensinact.gateway.common.automata;

/**
 * Describes a type of frame
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface FrameType 
{		
	/**
	 * Returns the length of the described frame
	 *  
	 * @return
	 * 		the length of of the described frame
	 */
	int length();
	
	/**
	 * Defines the length of the described frame
	 * 
	 * @param length
	 * 		the length of the described frame
	 */
	void setLength(int length);
	
	/**
	 * Returns the implementation class's name of the
	 * described frame
	 *  
	 * @return
	 * 		the implementation class's name of the
	 * 		described frame
	 */
	String getClassName();
	
	/**
	 * Defines the implementation class's name of the
	 * described frame
	 *  
	 * @param className
	 * 		the implementation class's name of the
	 * 		described frame
	 */
	void setClassName(String className);
	
	/**
	 * Returns the name of this type
	 *  
	 * @return
	 * 		this type's name 
	 */
	String getName();
	
	/**
	 * Defines the name of this type
	 *  
	 * @param name
	 * 		this type's name 
	 */
	void setName(String name);
}
