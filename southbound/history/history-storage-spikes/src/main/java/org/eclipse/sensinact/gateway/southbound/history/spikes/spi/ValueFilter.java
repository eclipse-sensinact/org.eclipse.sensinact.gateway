/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.spikes.spi;

import java.util.List;
import java.util.Objects;

/**
 * Declarative value predicate: the conjunction (AND) of simple comparisons
 * against typed literals.
 */
public record ValueFilter(List<Condition> conditions) {

    public enum Op {
        EQ, NE, LT, LE, GT, GE
    }

    public record Condition(Op op, Object literal) {
        public Condition {
            Objects.requireNonNull(op);
            Objects.requireNonNull(literal);
        }
    }

    public static ValueFilter of(Op op, Object literal) {
        return new ValueFilter(List.of(new Condition(op, literal)));
    }
}
