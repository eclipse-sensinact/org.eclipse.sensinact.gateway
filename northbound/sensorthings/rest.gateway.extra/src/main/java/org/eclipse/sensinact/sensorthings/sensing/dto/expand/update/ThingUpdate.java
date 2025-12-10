package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

@Service("admin")
public record ThingUpdate(@Model EClass model, @Provider String providerId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description,
        @Service("thing") @Resource("id") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object thingId,
        @Service("thing") @Resource("id") @Metadata(onMap = {
                USE_KEYS_AS_FIELDS }) Map<String, Object> properties,
        @Service("thing") @Resource("locationIds") @Data(onDuplicate = UPDATE_IF_DIFFERENT) List<String> locationIds)
        implements SensorThingsUpdate{

    public ThingUpdate {
        if (model == null) {
            model = SENSOR_THING_DEVICE;
        }
        if (model != SENSOR_THING_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_DEVICE.getName());
        }
        if (properties == null) {
            properties = Map.of();
        }
        if (locationIds == null) {
            locationIds = List.of();
        }
    }

    public ThingUpdate(String providerId, String friendlyName, String description, Object thingId,
            Map<String, Object> properties, List<String> locationIds) {
        this(SENSOR_THING_DEVICE, providerId, friendlyName, description, thingId, properties, locationIds);
    }

}