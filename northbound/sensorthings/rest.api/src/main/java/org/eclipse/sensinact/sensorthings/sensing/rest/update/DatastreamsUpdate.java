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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;

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
    public Response updateDatastreams(@PathParam("id") String id, ExpandedDataStream dataStream);

    @PATCH
    public Response patchDatastreams(@PathParam("id") String id, ExpandedDataStream dataStream);

    /**
     * update observation link to thing and datastream
     *
     * @param id
     * @param id2
     * @param observation
     * @return
     */
    @PUT
    @Path("/Observations({id2})")
    public Response updateDatastreamsObservation(@PathParam("id") String id, @PathParam("id2") String id2,
            Observation observation);

    @PATCH
    @Path("/Observations({id2})")
    public Response patchDatastreamsObservation(@PathParam("id") String id, @PathParam("id2") String id2,
            Observation observation);

    /**
     * update thing link to datastream
     *
     * @param id
     * @param id2
     * @param observation
     * @return
     */
    @PUT
    @Path("/Thing/$ref")
    public Response updateDatastreamThingRef(@PathParam("id") String id, RefId thing);

    /**
     * update sensor link to datastream
     *
     * @param id
     * @param id2
     * @param observation
     * @return
     */
    @PUT
    @Path("/Sensor/$ref")
    public Response updateDatastreamSensorRef(@PathParam("id") String id, RefId sensor);

    /**
     * update sensor link to datastream
     *
     * @param id
     * @param id2
     * @param observation
     * @return
     */
    @PUT
    @Path("/ObservedProperty/$ref")
    public Response updateDatastreamObservedPropertyRef(@PathParam("id") String id, RefId observedProperty);

}
