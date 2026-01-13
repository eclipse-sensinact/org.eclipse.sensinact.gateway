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
package org.eclipse.sensinact.sensorthings.sensing.rest;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin.SnapshotOption;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

public class UtilDto {

    public static String SERVICE_DATASTREAM = "datastream";
    public static String SERVICE_THING = "thing";
    public static String SERVICE_ADMIN = "admin";

    public static String SERVICE_LOCATON = "location";

    /**
     * get datastream service
     */
    public static ServiceSnapshot getDatastreamService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilDto.SERVICE_DATASTREAM);
    }

    public static Optional<ProviderSnapshot> getProviderSnapshot(SensiNactSession session, String id) {
        return Optional.ofNullable(session.providerSnapshot(id, EnumSet.noneOf(SnapshotOption.class)));
    }

    /**
     * get location service
     */
    public static ServiceSnapshot getLocationService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilDto.SERVICE_LOCATON);
    }

    /**
     * get thing device service
     */
    public static ServiceSnapshot getThingService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilDto.SERVICE_THING);
    }

    public static ServiceSnapshot getAdminService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilDto.SERVICE_ADMIN);
    }

    /**
     * return false if the class is not a record or the field doesn't exists else
     * true
     *
     * @param record
     * @param idFieldName
     * @return
     */
    public static boolean isRecordOnlyField(Object record, String idFieldName) {
        if (record == null || !record.getClass().isRecord()) {
            return false;
        }

        RecordComponent[] components = record.getClass().getRecordComponents();

        return Arrays.stream(components).allMatch(rc -> {
            try {
                Object value = rc.getAccessor().invoke(record);
                if (rc.getName().equals(idFieldName)) {
                    return value != null;
                } else {
                    return value == null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * return exception if record is not a record class else return the value of
     * field if it exists else null
     *
     * @param record
     * @param fieldName
     * @return
     *
     */
    public static Object getRecordField(Object record, String fieldName) {
        if (!record.getClass().isRecord()) {
            throw new IllegalArgumentException("Ce n'est pas un record !");
        }

        RecordComponent[] components = record.getClass().getRecordComponents();

        for (RecordComponent rc : components) {
            if (rc.getName().equals(fieldName)) {
                try {
                    Object value = rc.getAccessor().invoke(record);

                    return value;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    public static String extractIdSegment(String id, int part) {
        if (id.isEmpty())
            return null;
        String[] parts = id.split("~");
        if (parts == null || parts.length == 0) {
            return id;
        }
        if (parts == null || parts.length == 0)
            return id;
        if (part < parts.length) {
            return parts[part];
        }
        return null;
    }

    public static String extractFirstIdSegment(String id) {
        return extractIdSegment(id, 0);
    }

    public static <T> T getResourceField(ServiceSnapshot service, String resourceName, Class<T> expectedType) {

        var resource = service.getResource(resourceName);

        if (resource != null && resource.getValue() != null) {
            return expectedType.cast(resource.getValue().getValue());
        }
        if (List.class.isAssignableFrom(expectedType)) {
            return expectedType.cast(List.of());
        }

        if (Map.class.isAssignableFrom(expectedType)) {
            return expectedType.cast(Map.of());
        }
        return null;
    }

    public static String extractSecondIdSegment(String id) {
        return extractIdSegment(id, 1);

    }

    public static String extractThirdIdSegment(String id) {
        return extractIdSegment(id, 2);

    }
}
