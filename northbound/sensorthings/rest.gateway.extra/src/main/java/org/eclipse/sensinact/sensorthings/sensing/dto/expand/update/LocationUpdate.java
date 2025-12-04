package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.DATA_STREAM_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record LocationUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service String serviceName, Object id, String name, String description, String encodingType,
        GeoJsonObject location) implements SensorThingsUpdate {
    public LocationUpdate {
        if (model == null) {
            model = SENSOR_THINGS_DEVICE;
        }
        if (model != SENSOR_THINGS_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
        }
        if (service == null) {
            service = DATA_STREAM_SERVICE;// TODO change as it will be location service 
        }
        if (service != DATA_STREAM_SERVICE) {
            throw new IllegalArgumentException("The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
        }
    }

    public LocationUpdate(String providerId, String serviceName, Object sensorThingsId, String name, String description, String encodingType,
            GeoJsonObject location) {
        this(SENSOR_THINGS_DEVICE, DATA_STREAM_SERVICE, providerId, serviceName, sensorThingsId, name, description,
                encodingType,);
}