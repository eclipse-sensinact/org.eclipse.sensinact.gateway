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

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

public interface RootResourceCreate {
    /**
     * create datastream
     *
     * @param datastream
     * @return
     */
    @POST
    @Path("/Datastreams")
    public Response createDatastream(ExpandedDataStream datastream);

    /**
     * create observation
     *
     * @param observation
     * @return
     */
    @POST
    @Path("/Observations")
    public Response createObservations(ExpandedObservation observation);

    /**
     * create feature of interest
     *
     * @param featuresOfInterest
     * @return
     */
    @POST
    @Path("/FeaturesOfInterest")
    public Response createFeaturesOfInterest(FeatureOfInterest featuresOfInterest);

    /**
     * create location
     *
     * @param location
     * @return
     */
    @POST
    @Path("/Locations")
    public Response createLocation(ExpandedLocation location);

    /**
     * create observed Property
     *
     * @param observedProperty
     * @return
     */
    @POST
    @Path("/ObservedProperties")
    public Response createObservedProperties(ObservedProperty observedProperty);

    /**
     * create sensor
     *
     * @param sensor
     * @return
     */
    @POST
    @Path("/Sensors")
    public Response createSensors(Sensor sensor);

    /**
     * create a thing
     *
     * @param thing
     * @return
     */
    @POST
    @Path("/Things")
    public Response createThing(ExpandedThing thing);

}
