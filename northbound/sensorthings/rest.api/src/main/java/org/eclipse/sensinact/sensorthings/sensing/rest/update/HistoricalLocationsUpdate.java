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
package org.eclipse.sensinact.sensorthings.sensing.rest.update;

import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;

import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface HistoricalLocationsUpdate {
    /**
     * delete location and the link between location and thing if exists
     *
     * @param id : id of hitoricalLocation
     */
    @PUT
    public Response updateHistoricalLocation(@PathParam("id") ODataId id, HistoricalLocation hl);

    @PATCH
    public Response patchHistoricalLocation(@PathParam("id") ODataId id, HistoricalLocation hl);

}
