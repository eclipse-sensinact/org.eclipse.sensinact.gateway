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
*   Kentyou - restrict thread access
**********************************************************************/
package org.eclipse.sensinact.prototype.command.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.core.command.CommandScoped;

public abstract class CommandScopedImpl implements CommandScoped {

    protected final AtomicBoolean active;

    private final Thread creatingThread = Thread.currentThread();

    public CommandScopedImpl(AtomicBoolean active) {
        this.active = active;
    }

    public void invalidate() {
        active.set(false);
    }

    public boolean isValid() {
        return active.get();
    }

    protected void checkValid() {
        if (!active.get()) {
            throw new IllegalStateException("This scoped object has been closed");
        }
        if (!creatingThread.equals(Thread.currentThread())) {
            throw new IllegalStateException("This scoped object is being accessed from outside the creating thread");
        }
    }
}
