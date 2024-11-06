/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.filters.ldap.antlr.impl;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;

/**
 * Package URI criterion
 */
public class CriterionProviderPackageUri extends AbstractProviderCriterion {

    public CriterionProviderPackageUri(final IStringValue pkgUri, final LdapComparator comparator) {
        super("PACKAGE", pkgUri, comparator);
    }

    @Override
    protected String getProviderFieldValue(final ProviderSnapshot provider) {
        return provider.getModelPackageUri();
    }
}
