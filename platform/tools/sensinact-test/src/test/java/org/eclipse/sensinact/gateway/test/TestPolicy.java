/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.test;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TestPolicy extends java.security.Policy {
    private static final Permission ALL_PERMISSION = new AllPermission();

    private static final PermissionCollection PERMISSION_COLLECTION = new PermissionCollection() {

		private static final long serialVersionUID = 1L;
		final Vector<Permission> v = new Vector<Permission>();

        @Override
        public void add(Permission permission) {
        }

        @Override
        public Enumeration<Permission> elements() {
            v.addElement(ALL_PERMISSION);
            return v.elements();
        }

        @Override
        public boolean implies(Permission arg0) {
            return ALL_PERMISSION.implies(arg0);
        }
    };

    /**
     *
     */
    public TestPolicy() {
    }

    /**
     * @inheritDoc
     * @see java.security.Policy#implies(java.security.ProtectionDomain, java.security.Permission)
     */
    public boolean implies(ProtectionDomain domain, Permission permission) {
        return ALL_PERMISSION.implies(permission);
    }

    /**
     * @inheritDoc
     * @see java.security.Policy#getPermissions(java.security.CodeSource)
     */
    public PermissionCollection getPermissions(CodeSource codesource) {
        return PERMISSION_COLLECTION;
    }

    /**
     * @inheritDoc
     * @see java.security.Policy#getPermissions(java.security.ProtectionDomain)
     */
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return getPermissions(domain.getCodeSource());
    }

}
