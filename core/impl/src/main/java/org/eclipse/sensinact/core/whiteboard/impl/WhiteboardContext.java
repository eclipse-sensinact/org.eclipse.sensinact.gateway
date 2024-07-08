/*********************************************************************
* Copyright (c) 2024 Kentyou.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Thomas Calmant (Kentyou) - initial implementation
**********************************************************************/
package org.eclipse.sensinact.core.whiteboard.impl;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.sensinact.core.whiteboard.WhiteboardHandler;

class WhiteboardContext<T extends WhiteboardHandler<?>> {

    /**
     * The underlying handler
     */
    public final T handler;

    /**
     * The set of providers the handler was registered for
     */
    public final Set<String> providers = new HashSet<>();

    /**
     * The ID of the handler service
     */
    public final Long serviceId;

    public WhiteboardContext(Long svcId, T handler) {
        this.serviceId = svcId;
        this.handler = handler;
    }

    public WhiteboardContext(Long svcId, T handler, Set<String> providers) {
        this(svcId, handler);
        this.providers.addAll(providers);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) this.handler.getClass();
    }

    @Override
    public String toString() {
        return handler.getClass().getSimpleName() + "[serviceId=" + serviceId + ", providers=" + providers + "]";
    }
}
