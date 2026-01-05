package org.eclipse.sensinact.sensorthings.sensing.dto.expand.update;

import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;

public record ObservationUpdate(@Model EClass model, @ServiceModel EClass service, @Provider String providerId,
        @Service String serviceName, Object id, Instant phenomenonTime, Instant resultTime, Object result,
        Object resultQuality, TimeInterval validTime, Map<String, Object> parameters, String datastreamLink,
        String featureOfInterestLink, RefId datastream, RefId featureOfInterest) {

}
