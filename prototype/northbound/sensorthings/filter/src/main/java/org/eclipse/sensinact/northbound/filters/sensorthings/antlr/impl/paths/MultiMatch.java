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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.paths;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class MultiMatch<T> {

    public final List<T> entries;
    public final BiFunction<Object, Object, Boolean> equalityPredicate;

    public MultiMatch(final Collection<T> entries) {
        this(entries, Objects::equals);
    }

    public MultiMatch(final Collection<T> entries, final BiFunction<Object, Object, Boolean> predicate) {
        this.entries = List.copyOf(entries);
        this.equalityPredicate = predicate;
    }

    @Override
    public boolean equals(Object other) {
        return entries.stream().anyMatch(e -> equalityPredicate.apply(e, other));
    }

    /**
     * @return the entries
     */
    public List<T> getEntries() {
        return entries;
    }
}
