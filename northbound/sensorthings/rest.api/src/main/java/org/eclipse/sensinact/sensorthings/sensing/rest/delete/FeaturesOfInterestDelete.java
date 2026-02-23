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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface FeaturesOfInterestDelete {
    /**
     * delete feature of interest if not linked to observation - return 204 else
     * return 409
     *
     * @param id : id of a feature of interest
     */
    @DELETE
    public Response deleteFeatureOfInterest(@PathParam("id") ODataId id);

}
