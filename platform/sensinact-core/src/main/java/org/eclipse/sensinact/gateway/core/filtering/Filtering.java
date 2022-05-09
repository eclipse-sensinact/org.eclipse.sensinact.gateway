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
 * Filter to be applied on the response of an sensiNact's access method call
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Filtering {
	
	/*
	 * Name of the Component-property that is used to set the filter type(s)
	 */
	public static String TYPE= 	"sensinact.filtering.type";

	/**
	 * Returns true if this Filtering service is able to handle the String type of
	 * filter passed as parameter; returns false otherwise
	 * 
	 * @param type the String type of filter
	 * 
	 * @return
	 *         <ul>
	 *             <li>true if the specified type of filter is handled by this {@link Filtering} service</li>
	 *             <li>false otherwise</li>
	 *         </ul>
	 */
	boolean handle(String type);

	/**
	 * Returns the String formated LDAP component part of this Filtering service.
	 * This filter is used to discriminate the elements on which this Filtering
	 * service will be applied on
	 * 
	 * @param definition the String definition describing the Filtering service's parameters
	 * 
	 * @return the String formated LDAP component part of this Filtering service
	 */
	String getLDAPComponent(String definition);

	/**
	 * Applies the String filter passed as parameter on the Object also passed as
	 * parameter and returned the String filtered result, in the same format
	 * 
	 * @param definition the String definition describing the Filtering service's parameters
	 * @param obj the Object to be filtered
	 * 
	 * @return the String filtered result
	 */
	String apply(String definition, Object obj);
}
