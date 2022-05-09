/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.common.props;

import java.util.Set;

/**
 * A set of {@link TypedKey}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface KeysCollection {
    /**
     * Returns the array of this KeyCollection's key strings
     *
     * @return this KeyCollection's key strings
     */
    Set<TypedKey<?>> keys();

    /**
     * Returns the {@link TypedKey} whose name is passed
     * as parameter
     *
     * @return the {@link TypedKey} with the specified
     * name
     */
    TypedKey<?> key(String key);

}
