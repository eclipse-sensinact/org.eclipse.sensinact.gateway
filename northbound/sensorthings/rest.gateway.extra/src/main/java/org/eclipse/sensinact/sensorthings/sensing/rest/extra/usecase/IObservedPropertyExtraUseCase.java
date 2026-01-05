/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;

public interface IObservedPropertyExtraUseCase {
    /**
     * return cache observed property base on id else null
     *
     * @param id
     * @return
     */
    public ExpandedObservedProperty getInMemoryObservedProperty(String id);

    /**
     * remove cached observed property base oln id if exists
     *
     * @param id
     * @return
     */
    public ExpandedObservedProperty removeInMemoryObservedProperty(String id);

}
