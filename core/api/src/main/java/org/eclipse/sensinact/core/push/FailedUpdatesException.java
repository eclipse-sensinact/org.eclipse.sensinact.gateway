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
package org.eclipse.sensinact.core.push;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FailedUpdatesException extends Exception {

    private static final long serialVersionUID = 558905737809687311L;

    private final List<DataUpdateException> failedUpdates;

    public FailedUpdatesException(DataUpdateException failedUpdate) {
        Objects.requireNonNull(failedUpdate);
        failedUpdates = List.of(failedUpdate);
        addSuppressed(failedUpdate);
    }

    public FailedUpdatesException(Stream<? extends DataUpdateException> failedUpdates) {
        Objects.requireNonNull(failedUpdates);
        this.failedUpdates = failedUpdates.collect(toUnmodifiableList());
        this.failedUpdates.forEach(this::addSuppressed);
    }

    public List<DataUpdateException> getFailedUpdates() {
        return failedUpdates;
    }
}
