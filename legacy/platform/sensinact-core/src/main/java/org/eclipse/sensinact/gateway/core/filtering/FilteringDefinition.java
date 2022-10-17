/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.filtering;

/**
 * Gather the type and the String representation of an {@link Filtering} service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FilteringDefinition {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//
	
	public static final int UNRANKED = -1; 
	
	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * the type of filter
	 */
	public final String type;
	/**
	 * the String formated filter
	 */
	public final String filter;	
	/**
	 * the int rank of this filter definition 
	 */
	public final int rank;

	/**
	 * Constructor
	 * 
	 * @param type the type of filter defined by the FilterDefinition to be
	 * instantiated
	 * @param filter the String filter defined by the FilterDefinition to be
	 * instantiated
	 */
	public FilteringDefinition(String type, String filter) {
		this(type, filter, UNRANKED);
	}

	/**
	 * Constructor
	 * 
	 * @param type the type of filter defined by the FilterDefinition to be
	 * instantiated
	 * @param filter the String filter defined by the FilterDefinition to be
	 * instantiated
	 * @param rank the int rank of the FilterDefinition to be instantiated
	 */
	public FilteringDefinition(String type, String filter, int rank) {
		this.type = type;
		this.filter = filter;
		this.rank = rank;
	}
}
