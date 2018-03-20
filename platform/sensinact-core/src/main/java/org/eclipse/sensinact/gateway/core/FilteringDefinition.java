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
package org.eclipse.sensinact.gateway.core;

/**
 * Gather the type and the String representation of an {@link Filtering}
 * service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FilteringDefinition
{
	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//

	/**
	 * the type of filter
	 */
	public final String type;
	/**
	 * the String formated filter
	 */
	public final String filter;
	
	/**
	 * Constructor
	 * 
	 * @param type the type of filter defined by the 
	 * FilterDefinition to be instantiated
	 * @param filter the String filter defined by the 
	 * FilterDefinition to be instantiated
	 */
	public FilteringDefinition(String type, String filter)
	{
		this.type = type;
		this.filter = filter;
	}
}
