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

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface ThingsUpdate {
    /**
     * update datastream link to thing
     *
     * @param id
     * @param id2
     * @param datastream
     * @return
     */
    @PUT
    @PATCH
    @Path("/Datastreams({id2})")
    public Response updateDatastream(@PathParam("id") String id, @PathParam("id2") String id2,
            ExpandedDataStream datastream);

    /**
     * update location link to thing
     *
     * @param id
     * @param id2
     * @param location
     * @return
     */
    @PUT
    @PATCH
    @Path("/Locations({id2})")
    public Response updateLocation(@PathParam("id") String id, @PathParam("id2") String id2, ExpandedLocation location);

    /**
     * update thing
     *
     * @param id
     * @param thing
     * @return
     */
    @PUT
    @PATCH
    public Response updateThing(@PathParam("id") String id, ExpandedThing thing);

}
