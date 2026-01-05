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

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface DatastreamsCreate {

    /**
     * create observation link to datastream
     *
     * @param id
     * @param observation
     * @return
     */
    @POST
    @Path("/Observations")
    public Response createDatastreamsObservation(@PathParam("id") String id, ExpandedObservation observation);

    @POST
    @Path("/Sensor/$ref")
    public Response createDatastreamsLinkThing(@PathParam("id") String id, RefId ref);

    @POST
    @Path("/Thing/$ref")
    public Response createDatastreamsLinkSensor(@PathParam("id") String id, RefId ref);

    @POST
    @Path("/ObservedProperty/$ref")
    public Response createDatastreamsLinkObservedProperty(@PathParam("id") String id, RefId ref);

}
