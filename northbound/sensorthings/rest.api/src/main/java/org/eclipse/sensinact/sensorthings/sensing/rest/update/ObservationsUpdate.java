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

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;

import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface ObservationsUpdate {

    /**
     * update observation
     *
     * @param id
     * @param observation
     * @return
     */
    @PUT
    @Path("/Datastream/$ref")
    public Response updateObservationDatastreamRef(@PathParam("id") ODataId id, RefId datastream);

    /**
     * update observation
     *
     * @param id
     * @param observation
     * @return
     */
    @PUT
    public Response updateObservation(@PathParam("id") ODataId id, ExpandedObservation obs);

    /**
     * patch observation
     *
     * @param id
     * @param observation
     * @return
     */
    @PATCH
    public Response patchObservation(@PathParam("id") ODataId id, ExpandedObservation obs);

    /**
     * update observation
     *
     * @param id
     * @param observation
     * @return
     */
    @PUT
    @Path("/FeatureOfInterest/$ref")
    public Response updateObservationFeatureOfInterestRef(@PathParam("id") ODataId id, RefId foi);

}
