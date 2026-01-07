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
package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface ThingsDelete {
    /**
     * delete thing and linked entities
     *
     * @param id : id of thing
     */
    @DELETE
    public Response deleteThing(@PathParam("id") String id);

    /**
     * delete datastream thing ref
     *
     * @param id  : id of a thing
     * @param id2 : id of a datastream
     */
    @DELETE
    @Path("/Datastreams/$ref")
    public Response deleteDatastreamRef(@PathParam("id") String id, @QueryParam("id2") String id2);

    /**
     * delete location thing ref
     *
     * @param id : id of thing
     * @param id 2 : id of location
     */
    @DELETE
    @Path("/Locations({id2})/$ref")
    public Response deleteLocationRef(@PathParam("id") String id, @PathParam("id2") String id2);

    /**
     * delete locations thing ref
     *
     * @param id : id of thing
     */
    @DELETE
    @Path("/Locations/$ref")
    public Response deleteLocationsRef(@PathParam("id") String id);
}
