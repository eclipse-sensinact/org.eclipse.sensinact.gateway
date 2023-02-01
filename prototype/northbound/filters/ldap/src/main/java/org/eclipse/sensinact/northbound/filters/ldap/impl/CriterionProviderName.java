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
package org.eclipse.sensinact.northbound.filters.ldap.impl;

import java.util.function.Predicate;

import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;

public class CriterionProviderName extends AbstractCriterion {

    private final String providerName;

    public CriterionProviderName(final String providerName) {
        this.providerName = providerName;
    }

    @Override
    public String toString() {
        String content = String.format("(PROVIDER=%s)", providerName);
        if (isNegative()) {
            content = String.format("(!%s)", content);
        }
        return content;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        if (isNegative()) {
            return p -> !providerName.equals(p.getName());
        } else {
            return p -> providerName.equals(p.getName());
        }
    }
}
