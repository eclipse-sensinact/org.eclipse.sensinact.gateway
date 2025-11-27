package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

@Service("admin")
public record ThingUpdate(@Model EClass model, @Provider String providerId,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) GeoJsonObject location,
        @Service("thing") @Resource("id") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object thingId,
        @Service("thing") @Resource("id") @Metadata(onMap = {
                USE_KEYS_AS_FIELDS }) Map<String, Object> properties)
        implements SensorThingsUpdate{
    public ThingUpdate {
        if (model == null) {
            model = SENSOR_THINGS_DEVICE;
        }
        if (model != SENSOR_THINGS_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
        }
    }

    public ThingUpdate(String providerId, String friendlyName, String description, GeoJsonObject location,
            Object thingId, Map<String, Object> properties) {
        this(SENSOR_THINGS_DEVICE, providerId, friendlyName, description, location, thingId, properties);
    }
}