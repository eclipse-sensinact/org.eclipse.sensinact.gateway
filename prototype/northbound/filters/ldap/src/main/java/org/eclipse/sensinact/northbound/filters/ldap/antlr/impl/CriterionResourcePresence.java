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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;

/**
 * Resource presence filter
 */
public class CriterionResourcePresence extends AbstractCriterion {

    /**
     * Resource path
     */
    private final SensiNactPath rcPath;

    /**
     * @param rcPath Resource path
     */
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

        Predicate<ResourceSnapshot> isSet = r -> {
            if (r.getValue() == null || r.getValue().getTimestamp() == null) {
                return false;
            }

            final Object value = r.getValue().getValue();
            if (value == null) {
                return false;
            }

            if (value.getClass().isArray()) {
                return Array.getLength(value) != 0;
            }

            if (value instanceof Collection) {
                return !((Collection<?>) value).isEmpty();
            }
            return true;
        };

        if (isNegative()) {
            return (p, rs) -> rs.stream().anyMatch(r -> rcPath.accept(r) && !isSet.test(r));
        } else {
            return (p, rs) -> rs.stream().anyMatch(r -> rcPath.accept(r) && isSet.test(r));
        }
    }
}
