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
package org.eclipse.sensinact.gateway.nthbnd.filter.ldap;

import org.eclipse.sensinact.gateway.core.filtering.Filtering;
import org.osgi.service.component.annotations.Component;

/**
 * {@link Filtering} implementation allowing to apply an LDAP filter
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Component(immediate=true, service=Filtering.class, property="type=ldap")
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

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    
    @Override
    public boolean handle(String type) {
        return "ldap".equals(type);
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
