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

public interface ObservationsDelete {
    /**
     * delete observation
     *
     * @param id : id of observation
     */
    @DELETE
    public Response deleteObservation(@PathParam("id") String id);

    /**
     * delete feature of interest observation ref - return 409
     *
     * @param id : id of feature of interest
     */
    @DELETE
    @Path("/FeatureOfInterest/$ref")
    public Response deleteObservationFeatureOfInterest(@PathParam("id") String id);
}
