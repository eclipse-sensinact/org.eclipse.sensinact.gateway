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

import java.time.Duration;

/**
 * Interface that an auto-descriptive value resource handler must implement if
 * it expects the whiteboard to create its resource automatically
 *
 * @param <T> Type of resource
 */
public interface WhiteboardResourceDescription<T> extends WhiteboardGet<T> {

    /**
     * Returns the type of the resource to create
     */
    Class<T> getResourceType();

    /**
     * Returns the duration of the cache to avoid calling the handler too often
     */
    Duration getCacheDuration();
}
