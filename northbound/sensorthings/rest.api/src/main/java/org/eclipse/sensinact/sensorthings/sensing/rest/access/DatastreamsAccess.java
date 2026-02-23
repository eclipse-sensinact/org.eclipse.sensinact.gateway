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
package org.eclipse.sensinact.sensorthings.sensing.rest.access;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.eclipse.sensinact.sensorthings.sensing.rest.ODataId;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.RefFilter;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Produces(APPLICATION_JSON)
@Path("/v1.1/Datastreams({id})")
public interface DatastreamsAccess {

    @GET
    public Datastream getDatastream(@PathParam("id") ODataId id);

    @Path("{prop}")
    @GET
    @PropFilter
    default public Datastream getDatastreamProp(@PathParam("id") ODataId id) {
        return getDatastream(id);
    }

    @Path("{prop}/$value")
    @GET
    @PropFilter
    default public Datastream getDatastreamPropValue(@PathParam("id") ODataId id) {
        return getDatastream(id);
    }

    @Path("Observations")
    @GET
    public ResultList<Observation> getDatastreamObservations(@PathParam("id") ODataId id);

    @Path("Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Id> getDatastreamObservationsRef(@PathParam("id") ODataId id) {
        ResultList<Observation> datastreamObservations = getDatastreamObservations(id);
        return new ResultList<>(datastreamObservations.count(), datastreamObservations.nextLink(),
                datastreamObservations.value());
    }

    @Path("Observations({id2})/Datastream/Observations")
    @GET
    default public ResultList<Observation> getDatastreamObservationDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        String providerId = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!providerId.equals(id.value())) {
            throw new NotFoundException();
        }
        return getDatastreamObservations(id);
    }

    @Path("Observations({id2})/Datastream/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getDatastreamObservationDatastreamObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getDatastreamObservationDatastreamObservations(id, id2);
    }

    @Path("Observations({id2})")
    @GET
    public Observation getDatastreamObservation(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getDatastreamObservationDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationDatastreamSensor(id, id2);

    }

    @Path("Observations({id2})/Datastream/ObservedProperty")
    @GET
    default public ObservedProperty getDatastreamObservationDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        String idProvider = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!idProvider.equals(id.value())) {
            throw new NotFoundException();
        }
        return getDatastreamObservedProperty(id);
    }

    @Path("Observations({id2})/Datastream/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getDatastreamObservationDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationDatastreamObservedProperty(id, id2);

    }

    @Path("Observations({id2})/Datastream/Sensor")
    @GET
    default public Sensor getDatastreamObservationDatastreamSensor(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        String idProvider = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!idProvider.equals(id.value())) {
            throw new NotFoundException();
        }
        return getDatastreamSensor(id);
    }

    @Path("Observations({id2})/Datastream/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getDatastreamObservationDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationDatastreamThing(id, id2);

    }

    @Path("Observations({id2})/Datastream/Thing")
    @GET
    default public Thing getDatastreamObservationDatastreamThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        String idProvider = DtoMapperSimple.extractFirstIdSegment(id2.value());
        if (!idProvider.equals(id.value())) {
            throw new NotFoundException();
        }
        return getDatastreamThing(id);
    }

    @Path("Observations({id2})/$ref")
    @GET
    @RefFilter
    default public Observation getDatastreamObservationRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getDatastreamObservation(id, id2);

    }

    @Path("Observations({id2})/{prop}")
    @GET
    @PropFilter
    default public Observation getDatastreamObservationsProp(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservation(id, id2);
    }

    @Path("Observations({id2})/Datastream")
    @GET
    public Datastream getDatastreamObservationDatastream(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/Datastream/$ref")
    @GET
    @RefFilter
    default public Datastream getDatastreamObservationDatastreamRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationDatastream(id, id2);
    }

    @Path("Observations({id2})/FeatureOfInterest")
    @GET
    public FeatureOfInterest getDatastreamObservationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Observations({id2})/FeatureOfInterest/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getDatastreamObservationFeatureOfInterestObservationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getDatastreamObservationFeatureOfInterestObservations(id, id2);
    }

    @Path("Observations({id2})/FeatureOfInterest/Observations")
    @GET
    default public ResultList<Observation> getDatastreamObservationFeatureOfInterestObservations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return new ResultList<Observation>(null, null, List.of(getDatastreamObservation(id, id2)));
    }

    @Path("Observations({id2})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getDatastreamObservationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservationFeatureOfInterest(id, id2);
    }

    @Path("ObservedProperty")
    @GET
    public ObservedProperty getDatastreamObservedProperty(@PathParam("id") ODataId id);

    @Path("ObservedProperty/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamObservedPropertyRef(@PathParam("id") ODataId id) {
        return getDatastreamObservedProperty(id);
    }

    @Path("ObservedProperty/{prop}")
    @GET
    @PropFilter
    default public ObservedProperty getDatastreamObservedPropertyProp(@PathParam("id") ODataId id) {
        return getDatastreamObservedProperty(id);
    }

    @Path("ObservedProperty/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamObservedPropertyDatastreams(@PathParam("id") ODataId id);

    @Path("ObservedProperty/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getDatastreamObservedPropertyDatastreamsRef(@PathParam("id") ODataId id) {
        return getDatastreamObservedPropertyDatastreams(id);
    }

    @Path("Sensor")
    @GET
    public Sensor getDatastreamSensor(@PathParam("id") ODataId id);

    @Path("Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getDatastreamSensorRef(@PathParam("id") ODataId id) {
        return getDatastreamSensor(id);
    }

    @Path("Sensor/{prop}")
    @GET
    @PropFilter
    default public Sensor getDatastreamSensorProp(@PathParam("id") ODataId id) {
        return getDatastreamSensor(id);
    }

    @Path("Sensor/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamSensorDatastreams(@PathParam("id") ODataId id);

    @Path("Sensor/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getDatastreamSensorDatastreamsRef(@PathParam("id") ODataId id) {
        return getDatastreamSensorDatastreams(id);
    }

    @Path("Thing")
    @GET
    public Thing getDatastreamThing(@PathParam("id") ODataId id);

    @Path("Thing/$ref")
    @GET
    @RefFilter
    default public Self getDatastreamThingRef(@PathParam("id") ODataId id) {
        return getDatastreamThing(id);
    }

    @Path("Thing/Datastreams({id2})/Thing")
    @GET
    default public Thing getDatastreamThingDatastreamThing(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamThing(id2);
    }

    @Path("Thing/Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getDatastreamThingDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamThing(id, id2);
    }

    @Path("Thing/Datastreams({id2})/Observations")
    @GET
    public ResultList<Observation> getDatastreamThingDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Thing/Datastreams({id2})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getDatastreamThingDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamObservations(id, id2);
    }

    @Path("Thing/HistoricalLocations({id2})/Thing")
    @GET
    default public Thing getDatastreamThingHistoricalLocationThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        Thing thing = getDatastreamThing(id);
        if (!thing.id().equals(DtoMapperSimple.extractFirstIdSegment(id2.value()))) {
            throw new NotFoundException();
        }
        return thing;
    }

    @Path("Thing/HistoricalLocations({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getDatastreamThingHistoricalLocationThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingHistoricalLocationThing(id, id2);
    }

    @Path("Thing/HistoricalLocations({id2})/Locations")
    @GET
    default public ResultList<Location> getDatastreamThingHistoricalLocationLocations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        Thing thing = getDatastreamThing(id);
        if (!thing.id().equals(DtoMapperSimple.extractFirstIdSegment(id2.value()))) {
            throw new NotFoundException();
        }
        return getDatastreamThingLocations(id);

    }

    @Path("Thing/HistoricalLocations({id2})/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getDatastreamThingHistoricalLocationLocationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingHistoricalLocationLocations(id, id2);
    }

    @Path("Thing/Datastreams({id2})/Observations({id3})")
    @GET
    default public Observation getDatastreamThingDatastreamObservation(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservation(id, id3);
    }

    @Path("Thing/Datastreams({id2})/Observations({id3})/$ref")
    @GET
    @RefFilter
    default public Observation getDatastreamThingDatastreamObserationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getDatastreamThingDatastreamObservation(id, id2, id3);
    }

    @Path("Thing/Datastreams({id2})/Observations({id3})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getDatastreamThingDatastreamObservationFeatureOfInterest(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservationFeatureOfInterest(id2, id3);
    }

    @Path("Thing/Datastreams({id2})/Observations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getDatastreamThingDatastreamObserationFeatureOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        return getDatastreamThingDatastreamObservationFeatureOfInterest(id, id2, id3);
    }

    @Path("Thing/Datastreams({id2})/Sensor")
    @GET
    default public Sensor getDatastreamThingDatastreamSensor(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamSensor(id2);
    }

    @Path("Thing/Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getDatastreamThingDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamSensor(id, id2);
    }

    @Path("Sensor/Datastreams({id2})/Thing")
    @GET
    default public Thing getDatastreamSensorDatastreamThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamThing(id2);
    }

    @Path("Sensor/Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getDatastreamSensorDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamSensorDatastreamThing(id, id2);
    }

    @Path("Sensor/Datastreams({id2})/ObservedProperty")
    @GET
    default public ObservedProperty getSensorDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservedProperty(id2);
    }

    @Path("Sensor/Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getSensorDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getSensorDatastreamObservedProperty(id, id2);
    }

    @Path("Sensor/Datastreams({id2})/Observations")
    @GET
    default public ResultList<Observation> getSensorDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservations(id2);
    }

    @Path("Sensor/Datastreams({id2})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getSensorDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getSensorDatastreamObservations(id, id2);
    }

    @Path("Sensor/Datastreams({id2})/Observations({id3})")
    @GET
    default public Observation getSensorDatastreamObseration(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2,
            @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservation(id2, id3);
    }

    @Path("Sensor/Datastreams({id2})/Observations({id3})/$ref")
    @GET
    @RefFilter
    default public Observation getSensorDatastreamObserationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getSensorDatastreamObseration(id, id2, id3);
    }

    @Path("Sensor/Datastreams({id2})/Observations({id3})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getSensorDatastreamObserationFeatureOfInterest(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservationFeatureOfInterest(id2, id3);
    }

    @Path("Sensor/Datastreams({id2})/Observations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getSensorDatastreamObserationFeatureOfInterestRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getSensorDatastreamObserationFeatureOfInterest(id, id2, id3);
    }

    @Path("ObservedProperty/Datastreams({id2})/Observations")
    @GET
    default public ResultList<Observation> getObservedPropertyDatastreamObservations(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservations(id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/Observations/$ref")
    @GET
    @RefFilter
    default public ResultList<Observation> getObservedPropertyDatastreamObservationsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getObservedPropertyDatastreamObservations(id, id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/Observations({id3})")
    @GET
    default public Observation getObservedPropertyDatastreamObseration(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservation(id2, id3);
    }

    @Path("ObservedProperty/Datastreams({id2})/Observations({id3})/$ref")
    @GET
    @RefFilter
    default public Observation getObservedPropertyDatastreamObserationRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getObservedPropertyDatastreamObseration(id, id2, id3);
    }

    @Path("ObservedProperty/Datastreams({id2})/Observations({id3})/FeatureOfInterest")
    @GET
    default public FeatureOfInterest getObservedPropertyDatastreamObserationFeatureOfInterest(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getDatastreamObservationFeatureOfInterest(id2, id3);
    }

    @Path("ObservedProperty/Datastreams({id2})/Observations({id3})/FeatureOfInterest/$ref")
    @GET
    @RefFilter
    default public FeatureOfInterest getObservedPropertyDatastreamObserationFeatureOfInterestRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2, @PathParam("id3") ODataId id3) {
        // TODO add check
        return getObservedPropertyDatastreamObserationFeatureOfInterest(id, id2, id3);
    }

    @Path("Sensor/Datastreams({id2})/Sensor")
    @GET
    default public Sensor getSensorDatastreamSensor(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamSensor(id2);
    }

    @Path("Sensor/Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getSensorDatastreamSensorRef(@PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        // TODO add check
        return getSensorDatastreamSensor(id, id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/ObservedProperty")
    @GET
    default public ObservedProperty getObservedPropertyDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservedProperty(id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getObservedPropertyDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getObservedPropertyDatastreamObservedProperty(id, id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/Sensor")
    @GET
    default public Sensor getObservedPropertyDatastreamSensor(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamSensor(id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/Sensor/$ref")
    @GET
    @RefFilter
    default public Sensor getObservedPropertyDatastreamSensorRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getObservedPropertyDatastreamSensor(id, id2);
    }

    @Path("Thing/Datastreams({id2})/ObservedProperty")
    @GET
    default public ObservedProperty getDatastreamThingDatastreamObservedProperty(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamObservedProperty(id2);
    }

    @Path("Thing/Datastreams({id2})/ObservedProperty/$ref")
    @GET
    @RefFilter
    default public ObservedProperty getDatastreamThingDatastreamObservedPropertyRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingDatastreamObservedProperty(id, id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/Thing")
    @GET
    default public Thing getDatastreamObservedPropertyDatastreamThing(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        // TODO add check
        return getDatastreamThing(id2);
    }

    @Path("ObservedProperty/Datastreams({id2})/Thing/$ref")
    @GET
    @RefFilter
    default public Thing getDatastreamObservedPropertyDatastreamThingRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamObservedPropertyDatastreamThing(id, id2);
    }

    @Path("Thing/{prop}")
    @GET
    @PropFilter
    default public Thing getDatastreamThingProp(@PathParam("id") ODataId id) {
        return getDatastreamThing(id);
    }

    @Path("Thing/Datastreams")
    @GET
    public ResultList<Datastream> getDatastreamThingDatastreams(@PathParam("id") ODataId id);

    @Path("Thing/Datastreams/$ref")
    @GET
    @RefFilter
    default public ResultList<Datastream> getDatastreamThingDatastreamsRef(@PathParam("id") ODataId id) {
        return getDatastreamThingDatastreams(id);
    }

    @Path("Thing/HistoricalLocations")
    @GET
    public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocations(@PathParam("id") ODataId id);

    @Path("Thing/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getDatastreamThingHistoricalLocationsRef(
            @PathParam("id") ODataId id) {
        return getDatastreamThingHistoricalLocations(id);
    }

    @Path("Thing/Locations")
    @GET
    public ResultList<Location> getDatastreamThingLocations(@PathParam("id") ODataId id);

    @Path("Thing/Locations/$ref")
    @GET
    @RefFilter
    default public ResultList<Location> getDatastreamThingLocationsRef(@PathParam("id") ODataId id) {
        return getDatastreamThingLocations(id);
    }

    @Path("Thing/Locations({id2})/HistoricalLocations")
    @GET
    default public ResultList<HistoricalLocation> getDatastreamThingLocationHistoricalLocations(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getDatastreamThingHistoricalLocations(id);
    }

    @Path("Thing/Locations({id2})/HistoricalLocations/$ref")
    @GET
    @RefFilter
    default public ResultList<HistoricalLocation> getDatastreamThingLocationHistoricalLocationsRef(
            @PathParam("id") ODataId id, @PathParam("id2") ODataId id2) {
        return getDatastreamThingLocationHistoricalLocations(id, id2);
    }

    @Path("Thing/Locations({id2})/Things")
    @GET
    public ResultList<Thing> getDatastreamThingLocationThings(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2);

    @Path("Thing/Locations({id2})/Things/$ref")
    @GET
    @RefFilter
    default public ResultList<Thing> getDatastreamThingLocationThingsRef(@PathParam("id") ODataId id,
            @PathParam("id2") ODataId id2) {
        return getDatastreamThingLocationThings(id, id2);
    }

}
