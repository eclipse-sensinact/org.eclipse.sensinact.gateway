/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.filtering;

/**
 * Filter to be applied on the response of an sensiNact's access method call
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Filtering {
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
