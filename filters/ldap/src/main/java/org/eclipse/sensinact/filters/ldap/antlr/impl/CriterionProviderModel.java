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
package org.eclipse.sensinact.filters.ldap.antlr.impl;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;

/**
 * Model name criterion
 */
public class CriterionProviderModel extends AbstractProviderCriterion {

    public CriterionProviderModel(final IStringValue modelName, final LdapComparator comparator) {
        this(modelName, comparator, false);
    }

    private CriterionProviderModel(final IStringValue modelName, final LdapComparator comparator, boolean isNegated) {
        super("MODEL", modelName, comparator, isNegated);
    }

    @Override
    protected String getProviderFieldValue(final ProviderSnapshot provider) {
        return provider.getModelName();
    }

    @Override
    public ILdapCriterion negate() {
        return new CriterionProviderModel(expectedValue, comparator, !isNegative());
    }
}
