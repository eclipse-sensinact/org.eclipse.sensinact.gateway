/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.message;

/**
 * A MessageRegisterer is a recipient for {@link SnaMessage}s
 */
public interface MessageRegisterer {
    /**
     * Registers the {@link SnaMessage} passed as parameter
     *
     * @param message the {@link SnaMessage} to be registered
     */
    void register(SnaMessage<?> message);
}
