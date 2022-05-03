/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.filter.ldap;

import org.eclipse.sensinact.gateway.core.filtering.Filtering;
import org.eclipse.sensinact.gateway.core.filtering.FilteringType;
import org.osgi.service.component.annotations.Component;

/**
 * {@link Filtering} implementation allowing to apply an LDAP filter
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@FilteringType(LdapFiltering.LDAP)
@Component(immediate=true, service=Filtering.class)
public class LdapFiltering implements Filtering {

	//********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	public static final String LDAP = "ldap";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    
    @Override
    public boolean handle(String type) {
        return LDAP.equals(type);
    }

    @Override
    public String apply(String definition, Object result) {
        return String.valueOf(result);
    }

    @Override
    public String getLDAPComponent(String definition) {
        String ldapFilter = definition;
        if (ldapFilter.startsWith("'")) {
            ldapFilter = ldapFilter.substring(1);
        }
        if (ldapFilter.endsWith("'")) {
            ldapFilter = ldapFilter.substring(0, ldapFilter.length() - 1);
        }
        return ldapFilter;
    }
}
