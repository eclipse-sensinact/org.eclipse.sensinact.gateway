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

public abstract class NestableBuilderImpl<T, B, R> extends AbstractBuilderImpl<T> {

    protected final T parentBuilder;
    private final B builtParent;

    public NestableBuilderImpl(AtomicBoolean active, T parentBuilder, B builtParent) {
        super(active);
        this.parentBuilder = parentBuilder;
        this.builtParent = builtParent;
    }

    @SuppressWarnings("unchecked")
    public final T doBuild() {
        checkValid();
        doValidate();
        return parentBuilder != null ? parentBuilder : (T) doBuild(builtParent);
    }

    protected void doValidate() {

    }

    protected abstract R doBuild(B builtParent);
}
