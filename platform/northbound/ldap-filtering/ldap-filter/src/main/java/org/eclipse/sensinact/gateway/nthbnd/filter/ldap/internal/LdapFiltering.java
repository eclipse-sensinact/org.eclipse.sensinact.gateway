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
package org.eclipse.sensinact.gateway.nthbnd.filter.ldap.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;

/**
 * {@link Filtering} implementation allowing to apply an LDAP filter
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
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
    private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the
     *                 LdapFiltering to be instantiated to interact with
     *                 the OSGi host environment
     */
    public LdapFiltering(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#
     * handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        return "ldap".equals(type);
    }


    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#
     * apply(java.lang.String, java.lang.Object)
     */
    @Override
    public String apply(String definition, Object result) {
        return String.valueOf(result);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#getLDAPComponent()
     */
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
