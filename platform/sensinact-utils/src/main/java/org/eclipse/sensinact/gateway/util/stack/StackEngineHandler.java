/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.stack;

/**
 * Service connected to a {@link StackEngine}, in charge
 * of handling popped off elements
 *
 * @param <E> the type of handled element
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface StackEngineHandler<E> {
    /**
     * Handles the element popped off from the connected
     * {@link StackEngine}
     *
     * @param element the popped off <code>&lt;E&gt;</code> element
     *                to handle
     */
    void doHandle(E element);
}
