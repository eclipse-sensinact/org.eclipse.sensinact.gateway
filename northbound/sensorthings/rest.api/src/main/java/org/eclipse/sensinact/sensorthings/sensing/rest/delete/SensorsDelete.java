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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface SensorsDelete {
    /**
     * delete sensor if not link to datastream and exists - return 204 else return
     * 409. 404 if not found
     *
     * @param id : id of sensor
     */
    @DELETE
    public Response deleteSensor(@PathParam("id") String id);

}
