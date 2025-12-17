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

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;

import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface FeaturesOfInterestUpdate {
    /**
     * update feature of interest
     *
     * @param id
     * @param observation
     * @return
     */
    @PUT
    @PATCH
    public Response updateFeaturesOfInterest(@PathParam("id") String id, FeatureOfInterest observation);

    /**
     * update feature of interest link to observations
     *
     * @param id
     * @param id2
     * @param observation
     * @return
     */
    @PUT
    @PATCH
    @Path("/Observations({id2})")
    public Response updateFeaturesOfInterestObservation(@PathParam("id") String id, @PathParam("id2") String id2,
            Observation observation);

}
