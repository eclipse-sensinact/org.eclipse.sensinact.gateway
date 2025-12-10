package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_LOCATION;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_LOCATION_SERVICE;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

/**
 * Update record for the Location of a SensorThing / Device
 */
@Service("admin")
public record LocationUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service("locations") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object sensorThingId,
        @Service("locations") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String name,
        @Service("locations") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String description,
        @Service("locations") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) String encodingType,
        @Service("locations") @Data(onDuplicate = UPDATE_IF_DIFFERENT, onNull = UPDATE_IF_PRESENT) GeoJsonObject location)
        implements SensorThingsUpdate {

    public LocationUpdate {
        if (model == null) {
            model = SENSOR_THING_LOCATION;
        }
        if (model != SENSOR_THING_LOCATION) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_LOCATION.getName());
        }
        if (service == null) {
            service = SENSOR_THING_LOCATION_SERVICE;
        }
        if (service != SENSOR_THING_LOCATION_SERVICE) {
            throw new IllegalArgumentException(
                    "The model for the thing must be " + SENSOR_THING_LOCATION_SERVICE.getName());
        }
    }

    public LocationUpdate(String providerId, Object sensorThingsId, String name, String description,
            String encodingType, GeoJsonObject location) {
        this(SENSOR_THING_LOCATION, SENSOR_THING_LOCATION_SERVICE, providerId, sensorThingsId, name, description,
                encodingType, location);
    }
}
