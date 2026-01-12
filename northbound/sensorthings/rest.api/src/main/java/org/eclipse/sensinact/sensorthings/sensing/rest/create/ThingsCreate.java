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
package org.eclipse.sensinact.sensorthings.sensing.rest.create;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface ThingsCreate {

    /**
     * create datastream link to thing
     *
     * @param id
     * @param datastream
     * @return
     */
    @POST
    @Path("/Datastreams")
    public Response createDatastream(@PathParam("id") String id, ExpandedDataStream datastream);

    /**
     * create location link to thing
     *
     * @param id
     * @param location
     * @return
     */
    @POST
    @Path("/Locations")
    public Response createLocation(@PathParam("id") String id, ExpandedLocation location);

    /**
     * update location ref
     *
     * @param id
     * @param id2
     * @param location
     * @return
     */
    @POST
    @Path("/Locations/$ref")
    public Response updateLocationRef(@PathParam("id") String id, RefId location);

    /**
     * update datastream ref
     *
     * @param id
     * @param id2
     * @param location
     * @return
     */
    @POST
    @Path("/Datastreams/$ref")
    public Response updateDatastreamRef(@PathParam("id") String id, RefId datastream);

}
