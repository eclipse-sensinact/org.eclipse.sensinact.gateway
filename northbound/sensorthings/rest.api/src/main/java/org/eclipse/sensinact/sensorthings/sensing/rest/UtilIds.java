package org.eclipse.sensinact.sensorthings.sensing.rest;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;

public class UtilIds {

    public static String SERVICE_DATASTREAM = "datastream";
    public static String SERVICE_THING = "thing";
    public static String SERVICE_LOCATON = "location";

    public static ServiceSnapshot getDatastreamService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilIds.SERVICE_DATASTREAM);
    }

    public static ServiceSnapshot getLocationService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilIds.SERVICE_LOCATON);
    }

    public static ServiceSnapshot getThingService(ProviderSnapshot providerDatastream) {
        return providerDatastream.getService(UtilIds.SERVICE_THING);
    }

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

        return service.getResource(resourceName) != null && service.getResource(resourceName).getValue() != null
                ? expectedType.cast(service.getResource(resourceName).getValue().getValue())
                : null;
    }

    public static String extractSecondIdSegment(String id) {
        return extractIdSegment(id, 1);

    }

    public static String extractThirdIdSegment(String id) {
        return extractIdSegment(id, 2);

    }
}
