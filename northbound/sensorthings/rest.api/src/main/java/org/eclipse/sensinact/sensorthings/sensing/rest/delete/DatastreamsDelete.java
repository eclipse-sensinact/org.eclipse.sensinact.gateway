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

import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface DatastreamsDelete {

    /**
     * delete datastream
     *
     * @param id : id of a datastream
     */
    @DELETE
    public Response deleteDatastream(@PathParam("id") ODataId id);

    /**
     * delete datastream sensor ref - return 409
     *
     * @param id : id of a datastream
     */
    @DELETE
    @Path("/Sensor/$ref")
    public Response deleteDatastreamSensorRef(@PathParam("id") ODataId id);

    /**
     * delete datastream observed property ref - return 409
     *
     * @param id : id of a datastream
     *
     */
    @DELETE
    @Path("/ObservedProperty/$ref")
    public Response deleteDatastreamObservedPropertyRef(@PathParam("id") ODataId id);

    /**
     * delete datastream last observation ref - return 409
     *
     * @param id : id of a datastream
     *
     */
    @DELETE
    @Path("/Datastreams({id})/Observations/$ref")
    public Response deleteDatastreamObservationsRef(@PathParam("id") ODataId id);

}
