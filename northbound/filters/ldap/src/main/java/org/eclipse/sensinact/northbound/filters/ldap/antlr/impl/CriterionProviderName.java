/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.filters.ldap.antlr.impl;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;

/**
 * Provider name criterion
 */
public class CriterionProviderName extends AbstractProviderCriterion {

    public CriterionProviderName(final IStringValue providerName, final LdapComparator comparator) {
        super("PROVIDER", providerName, comparator);
    }

    @Override
    protected String getProviderFieldValue(final ProviderSnapshot provider) {
        return provider.getName();
    }
}
