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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;

/**
 * provide method to get Dto object from provider/service/resource
 */
public class ServiceSnapshotMapper {

    /*
     * ============================ Sensor ============================
     */
    public static ExpandedSensor toSensor(ServiceSnapshot service) {
        String id = UtilDto.getResourceField(service, "sensorId", String.class);
        String name = UtilDto.getResourceField(service, "sensorName", String.class);
        String description = UtilDto.getResourceField(service, "sensorDescription", String.class);
        String encodingType = UtilDto.getResourceField(service, "sensorEncodingType", String.class);
        Object metadata = UtilDto.getResourceField(service, "sensorMetadata", Object.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = UtilDto.getResourceField(service, "sensorProperties", Map.class);

        return new ExpandedSensor(null, id, name, description, encodingType, metadata, properties, null);
    }

    /*
     * ============================ Observation ============================
     */
    public static ExpandedObservation toObservation(ServiceSnapshot service) {
        return UtilDto.getResourceField(service, "lastObservation", ExpandedObservation.class);

    }

    /*
     * ============================ ObservedProperty ============================
     */
    public static ExpandedObservedProperty toObservedProperty(ServiceSnapshot service) {
        String id = UtilDto.getResourceField(service, "observedPropertyId", String.class);
        String name = UtilDto.getResourceField(service, "observedPropertyName", String.class);
        String description = UtilDto.getResourceField(service, "observedPropertyDescription", String.class);
        String definition = UtilDto.getResourceField(service, "observedPropertyDefinition", String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = UtilDto.getResourceField(service, "observedPropertyProperties", Map.class);

        return new ExpandedObservedProperty(null, id, name, description, definition, properties, null);
    }

    /*
     * ============================ UnitOfMeasurement ============================
     */
    public static UnitOfMeasurement toUnitOfMeasurement(ServiceSnapshot service) {
        String name = UtilDto.getResourceField(service, "observedPropertyName", String.class);
        String symbol = UtilDto.getResourceField(service, "observedPropertyDescription", String.class);
        String definition = UtilDto.getResourceField(service, "observedPropertyEncodingType", String.class);

        return new UnitOfMeasurement(name, symbol, definition);
    }

    /*
     * ============================ Datastream ============================
     */
    public static ExpandedDataStream toDatastream(ProviderSnapshot provider) {
        ServiceSnapshot serviceAdmin = UtilDto.getAdminService(provider);
        ServiceSnapshot service = UtilDto.getDatastreamService(provider);

        String id = UtilDto.getResourceField(service, "id", String.class);
        String name = UtilDto.getResourceField(serviceAdmin, "name", String.class);
        String description = UtilDto.getResourceField(serviceAdmin, "description", String.class);
        String observationType = UtilDto.getResourceField(service, "observationType", String.class);

        Geometry observedArea = UtilDto.getResourceField(service, "observedArea", Geometry.class);
        TimeInterval phenomenonTime = UtilDto.getResourceField(service, "phenomenonTime", TimeInterval.class);
        TimeInterval resultTime = UtilDto.getResourceField(service, "resultTime", TimeInterval.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = UtilDto.getResourceField(service, "properties", Map.class);

        UnitOfMeasurement uom = toUnitOfMeasurement(service);
        ExpandedObservedProperty observedProperty = toObservedProperty(service);
        ExpandedSensor sensor = toSensor(service);

        return new ExpandedDataStream(null, id, name, description, observationType, uom, observedArea, phenomenonTime,
                resultTime, properties, null, null, null, null, null, observedProperty, sensor, null, null);
    }

    /*
     * ============================ FeatureOfInterest ============================
     */
    public static FeatureOfInterest toFeatureOfInterest(ProviderSnapshot provider) {
        ServiceSnapshot serviceAdmin = UtilDto.getAdminService(provider);
        ServiceSnapshot service = UtilDto.getDatastreamService(provider);

        String name = UtilDto.getResourceField(serviceAdmin, "foiName", String.class);
        String description = UtilDto.getResourceField(serviceAdmin, "foiDescription", String.class);
        String encodingType = UtilDto.getResourceField(service, "foiEncodingType", String.class);
        GeoJsonObject feature = UtilDto.getResourceField(service, "foiFeature", GeoJsonObject.class);

        return new FeatureOfInterest(null, null, name, description, encodingType, feature, null);
    }

    /*
     * ============================ Location (Thing ↔ Location)
     * ============================
     */
    public static ExpandedLocation toLocation(ServiceSnapshot serviceLocation, String thingId) {
        String id = UtilDto.getResourceField(serviceLocation, "id", String.class);

        return new ExpandedLocation(null, id, null, null, null, null, null, null, List.of(new RefId(thingId)));
    }
}
