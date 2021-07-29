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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * A FilteringCollection wraps a set of {@link FilteringAccessor}s, allowing on
 * one hand to build the global LDAP formated String filter from the ones
 * provided by each {@link FilteringAccessor}, on the other hand it can be used
 * to apply all the filters, using the output of one as input of the next.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class FilteringCollection {
	private Iterable<FilteringAccessor> accessors;
	private boolean hideFilter;
	private String filterJsonDefinition;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing the FilteringCollection to be
	 *            instantiated to interact with the OSGi host environment
	 * @param hideFilter
	 *            the boolean defining whether the JSON formated String describing
	 *            the {@link FilteringAccessor}s wrapped by the FilteringCollection
	 *            to be instantiated will defined as hidden
	 * @param filterDefinition
	 *            the set of {@link FilteringDefinition}s from which the
	 *            FilteringCollection to be instantiated will create the set of
	 *            {@link FilteringAccessor}s
	 */
	public FilteringCollection(Mediator mediator, boolean hideFilter, FilteringDefinition... filterDefinition) {
		if (filterDefinition == null || filterDefinition.length == 0) {
			this.hideFilter = true;
			this.accessors = null;
			return;
		}
		int index = 0;
		int length = filterDefinition.length;

		StringBuilder jsonFormatedFilterBuilder = new StringBuilder();
		jsonFormatedFilterBuilder.append("[");

		List<FilteringAccessor> accessors = new ArrayList<FilteringAccessor>();

		int pos = 0;

		for (; index < length; index++) {
			try {
				accessors.add(new FilteringAccessor(mediator, filterDefinition[index]));

				if (pos > 0) {
					jsonFormatedFilterBuilder.append(",");
				}
				jsonFormatedFilterBuilder.append(String.format("{\"type\":\"%s\",\"definition\":\"%s\"}",
						filterDefinition[index].type, filterDefinition[index].filter));

				pos++;

			} catch (RuntimeException e) {
				mediator.error(e);
			}
		}
		if (index != length) {
			this.filterJsonDefinition = "[]";
			this.hideFilter = true;
			this.accessors = null;
			return;
		}
		jsonFormatedFilterBuilder.append("]");
		this.filterJsonDefinition = jsonFormatedFilterBuilder.toString();
		this.accessors = accessors;
	}

	/**
	 * Builds and returns the global LDAP formated String filter from the ones
	 * provided by each registered {@link FilteringAccessor}
	 * 
	 * @param ldapFilter
	 *            the initial LDAP formated String filter from which to create the
	 *            global one
	 * 
	 * @return the global LDAP formated String filter
	 */
	public String composeLDAPFormatedFilter(String ldapFilter) {
		String ldap = null;
		int index = 0;

		StringBuilder ldapFormatedFilterBuilder = new StringBuilder();
		if (ldapFilter != null) {
			ldap = ldapFilter.trim();
			if (!ldap.startsWith("(")) {
				ldapFormatedFilterBuilder.append("(");
			}
			ldapFormatedFilterBuilder.append(ldap);
			if (!ldap.endsWith(")")) {
				ldapFormatedFilterBuilder.append(")");
			}
			index++;
		}
		if (accessors == null) {
			this.hideFilter = true;
			return ldapFormatedFilterBuilder.toString();
		}
		Iterator<FilteringAccessor> it = accessors.iterator();
		while (it.hasNext()) {
			FilteringAccessor filteringAccessor = it.next();
			ldap = filteringAccessor.getLDAPComponent();
			if (ldap == null || (ldap = ldap.trim()).length() == 0) {
				continue;
			}
			if (!ldap.startsWith("(")) {
				ldapFormatedFilterBuilder.append("(");
			}
			ldapFormatedFilterBuilder.append(ldap);
			if (!ldap.endsWith(")")) {
				ldapFormatedFilterBuilder.append(")");
			}
			if (index > 0) {
				ldapFormatedFilterBuilder.insert(0, "(&");
				ldapFormatedFilterBuilder.append(")");
			}
			index++;
		}
		return ldapFormatedFilterBuilder.toString();
	}

	/**
	 * Applies each {@link FilteringAccessor} in order, by using the output of one
	 * as input of the next, the first input being the specified initial String
	 * value.
	 * 
	 * @param value
	 *            the String value to be filtered
	 * 
	 * @return the String result of the filtering process
	 */
	public String apply(String value) {
		String result = value;

		if (accessors != null) {
			Iterator<FilteringAccessor> it = accessors.iterator();
			while (it.hasNext()) {
				FilteringAccessor filtering = it.next();
				result = filtering.apply(result);
			}
		}
		return result;
	}

	/**
	 * Returns true if the the JSON formated String describing the
	 * {@link FilteringAccessor}s wrapped by this FilteringCollection is hidden;
	 * returns false otherwise
	 * 
	 * @return
	 *         <ul>
	 *         <li>true if the the JSON formated String describing the wrapped
	 *         {@link FilteringAccessor}s is hidden</li>
	 *         <li>false otherwise</li>
	 *         </ul>
	 */
	public boolean hideFilter() {
		return this.hideFilter;
	}

	/**
	 * Returns the global JSON formated String description of the filters provided
	 * by each registered {@link FilteringAccessor}
	 * 
	 * @return the global JSON formated String filter
	 */
	public String filterJsonDefinition() {
		return this.filterJsonDefinition;
	}
}
