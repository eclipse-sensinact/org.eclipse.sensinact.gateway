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
package org.eclipse.sensinact.prototype.model.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.prototype.command.impl.CommandScopedImpl;

public abstract class AbstractBuilderImpl<T> extends CommandScopedImpl {

    private boolean built = false;

    public AbstractBuilderImpl(AtomicBoolean active) {
        super(active);
    }

    public final T build() {
        if (built) {
            throw new IllegalStateException("This builder has already been built");
        }
        this.built = true;
        return doBuild();
    }

    protected abstract T doBuild();
}
