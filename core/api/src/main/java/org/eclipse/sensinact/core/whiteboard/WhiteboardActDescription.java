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
package org.eclipse.sensinact.core.whiteboard;

import java.util.List;
import java.util.Map.Entry;

/**
 * Interface that an auto-descriptive action resource handler must implement if
 * it expects the whiteboard to create its resource automatically
 *
 * @param <T> Type of action result
 */
public interface WhiteboardActDescription<T> {

    /**
     * Type of the result returns by the action
     */
    Class<T> getReturnType();

    /**
     * List of parameters: argument name -&gt; argument type
     */
    List<Entry<String, Class<?>>> getNamedParameterTypes();
}
