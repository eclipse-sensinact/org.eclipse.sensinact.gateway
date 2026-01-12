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
import jakarta.ws.rs.core.Response;

public interface LocationsDelete {
    /**
     * delete location and the link between location and thing if exists
     *
     * @param id : id of location
     */
    @DELETE
    public Response deleteLocation(@PathParam("id") String id);

    /**
     * delete things Location ref
     *
     * @param id : id of location
     */
    @DELETE
    @Path("/Things/$ref")
    public Response deleteThingsRef(@PathParam("id") String id);

    /**
     * delete things Location ref
     *
     * @param id  : id of location
     * @param id2 : id of Thing
     *
     */
    @DELETE
    @Path("/Things({id2})/$ref")
    public Response deleteThingRef(@PathParam("id") String id, @PathParam("id2") String id2);

}
