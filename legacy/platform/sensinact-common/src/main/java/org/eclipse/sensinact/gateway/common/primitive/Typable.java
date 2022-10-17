/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.primitive;

/**
 * A typed service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Typable<T extends Enum<T>> {
    /**
     * Returns the <code>&lt;T&gt;</code> Enum type
     * instance of this Typable service
     *
     * @return the <code>&lt;T&gt;</code> Enum type
     * of this Typable service
     */
    T getType();
}
