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

import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;

/**
 * Resource presence filter
 */
public class CriterionResourcePresence extends AbstractCriterion {

    private final SensiNactPath rcPath;

    public CriterionResourcePresence(final SensiNactPath rcPath) {
        this.rcPath = rcPath;
    }

    @Override
    public String toString() {
        String content = String.format("(%s=*)", rcPath);
        if (isNegative()) {
            content = String.format("(!%s)", content);
        }
        return content;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        if (isNegative()) {
            return (p, rs) -> rs.stream()
                    .anyMatch(r -> rcPath.accept(r) && (r.getValue() == null || r.getValue().getTimestamp() == null));
        } else {
            return (p, rs) -> rs.stream()
                    .anyMatch(r -> rcPath.accept(r) && (r.getValue() != null && r.getValue().getTimestamp() != null));
        }
    }
}
