package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.mapper;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilIds;

public class ServiceSnapshotMapper {

    /*
     * ============================ Sensor ============================
     */
    public static ExpandedSensor toSensor(ServiceSnapshot service) {
        String id = UtilIds.getResourceField(service, "sensorId", String.class);
        String name = UtilIds.getResourceField(service, "sensorName", String.class);
        String description = UtilIds.getResourceField(service, "sensorDescription", String.class);
        String encodingType = UtilIds.getResourceField(service, "sensorEncodingType", String.class);
        Object metadata = UtilIds.getResourceField(service, "sensorMetadata", Object.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = UtilIds.getResourceField(service, "sensorProperties", Map.class);

        return new ExpandedSensor(null, id, name, description, encodingType, metadata, properties, null);
    }

    /*
     * ============================ ObservedProperty ============================
     */
    public static ExpandedObservedProperty toObservedProperty(ServiceSnapshot service) {
        String id = UtilIds.getResourceField(service, "observedPropertyId", String.class);
        String name = UtilIds.getResourceField(service, "observedPropertyName", String.class);
        String description = UtilIds.getResourceField(service, "observedPropertyDescription", String.class);
        String definition = UtilIds.getResourceField(service, "observedPropertyEncodingType", String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = UtilIds.getResourceField(service, "observedPropertyProperties", Map.class);

        return new ExpandedObservedProperty(null, id, name, description, definition, properties, null);
    }

    /*
     * ============================ UnitOfMeasurement ============================
     */
    public static UnitOfMeasurement toUnitOfMeasurement(ServiceSnapshot service) {
        String name = UtilIds.getResourceField(service, "observedPropertyName", String.class);
        String symbol = UtilIds.getResourceField(service, "observedPropertyDescription", String.class);
        String definition = UtilIds.getResourceField(service, "observedPropertyEncodingType", String.class);

        return new UnitOfMeasurement(name, symbol, definition);
    }

    /*
     * ============================ Datastream ============================
     */
    public static ExpandedDataStream toDatastream(ServiceSnapshot service) {
        String id = UtilIds.getResourceField(service, "id", String.class);
        String name = UtilIds.getResourceField(service, "name", String.class);
        String description = UtilIds.getResourceField(service, "description", String.class);
        String observationType = UtilIds.getResourceField(service, "observationType", String.class);

        Geometry observedArea = UtilIds.getResourceField(service, "observedArea", Geometry.class);
        TimeInterval phenomenonTime = UtilIds.getResourceField(service, "phenomenonTime", TimeInterval.class);
        TimeInterval resultTime = UtilIds.getResourceField(service, "resultTime", TimeInterval.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = UtilIds.getResourceField(service, "properties", Map.class);

        UnitOfMeasurement uom = toUnitOfMeasurement(service);
        ExpandedObservedProperty observedProperty = toObservedProperty(service);
        ExpandedSensor sensor = toSensor(service);

        return new ExpandedDataStream(null, id, name, description, observationType, uom, observedArea, phenomenonTime,
                resultTime, properties, null, null, null, null, null, observedProperty, sensor, null, null);
    }

    /*
     * ============================ FeatureOfInterest ============================
     */
    public static FeatureOfInterest toFeatureOfInterest(ServiceSnapshot service) {
        String name = UtilIds.getResourceField(service, "foiName", String.class);
        String description = UtilIds.getResourceField(service, "foiDescription", String.class);
        String encodingType = UtilIds.getResourceField(service, "foiEncodingType", String.class);
        GeoJsonObject feature = UtilIds.getResourceField(service, "foiFeature", GeoJsonObject.class);

        return new FeatureOfInterest(null, null, name, description, encodingType, feature, null);
    }

    /*
     * ============================ Location (Thing ↔ Location)
     * ============================
     */
    public static ExpandedLocation toLocation(ServiceSnapshot serviceLocation, String thingId) {
        String id = UtilIds.getResourceField(serviceLocation, "id", String.class);

        return new ExpandedLocation(null, id, null, null, null, null, null, null, List.of(new RefId(thingId)));
    }
}
