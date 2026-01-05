package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.DATA_STREAM_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_DEVICE;
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.Literals.SENSOR_THING_SENSOR;

import java.time.Instant;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;

public record FeatureOfInterestUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service String serviceName, FeatureOfInterest featureOfInterest,
        @Data(onDuplicate = UPDATE_IF_DIFFERENT) String type) implements SensorThingsUpdate {

    public FeatureOfInterestUpdate {
        if (model == null) {
            model = SENSOR_THING_SENSOR;
        }
        if (model != SENSOR_THING_DEVICE) {
            throw new IllegalArgumentException("The model for the provider must be " + SENSOR_THING_DEVICE.getName());
        }
        if (service == null) {
            service = DATA_STREAM_SERVICE;
        }
        if (service != DATA_STREAM_SERVICE) {
            throw new IllegalArgumentException("The model for the datastream must be " + DATA_STREAM_SERVICE.getName());
        }
    }

    public FeatureOfInterestUpdate(String providerId, String serviceName, FeatureOfInterest featureOfInterest) {
        this(SENSOR_THING_DEVICE, DATA_STREAM_SERVICE, providerId, serviceName, featureOfInterest,
                DATA_STREAM_SERVICE.getInstanceClass().getSimpleName());
    }

}
