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

import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;

import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface DatastreamsUpdate {

    /**
     * update datastream link to thing
     *
     * @param id
     * @param dataStream
     * @return
     */
    @PUT
    @PATCH
    public Response updateDatastreams(@PathParam("id") String id, ExpandedDataStream dataStream);

    /**
     * update observation link to thing and datastream
     *
     * @param id
     * @param id2
     * @param observation
     * @return
     */
    @PUT
    @PATCH
    @Path("/Observations({id2})")
    public Response updateDatastreamsObservation(@PathParam("id") String id, @PathParam("id2") String id2,
            Observation observation);

}
