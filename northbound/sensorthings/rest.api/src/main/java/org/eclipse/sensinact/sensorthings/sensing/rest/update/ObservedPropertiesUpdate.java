/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.rest.update;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;

import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface ObservedPropertiesUpdate {
    /**
     * update observed property
     *
     * @param id
     * @param observedProperty
     * @return
     */
    @PUT
    public Response updateObservedProperties(@PathParam("id") String id, ExpandedObservedProperty observedProperty);

    @PATCH
    public Response patchObservedProperties(@PathParam("id") String id, ExpandedObservedProperty observedProperty);

}
