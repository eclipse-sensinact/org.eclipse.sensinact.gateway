/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.extract.impl;

import static org.eclipse.sensinact.prototype.annotation.dto.AnnotationConstants.NOT_SET;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.MapAction;
import org.eclipse.sensinact.prototype.annotation.dto.Metadata;
import org.eclipse.sensinact.prototype.annotation.dto.Model;
import org.eclipse.sensinact.prototype.annotation.dto.NullAction;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Resource;
import org.eclipse.sensinact.prototype.annotation.dto.Service;
import org.eclipse.sensinact.prototype.annotation.dto.Timestamp;
import org.eclipse.sensinact.prototype.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;

public class AnnotationMapping {

    static Function<Object, List<? extends AbstractUpdateDto>> getUpdateDtoMappings(Class<?> clazz) {
        Map<Field, Data> dataFields = getAnnotatedFields(clazz, Data.class);
        Map<Field, Metadata> metadataFields = getAnnotatedFields(clazz, Metadata.class);

        Function<Object, Instant> timestamp = getTimestampMapping(clazz);

        List<Function<Object, ? extends AbstractUpdateDto>> list = new ArrayList<>();

        for (Entry<Field, Data> e : dataFields.entrySet()) {
            list.add(createDataMapping(clazz, e.getKey(), e.getValue()));
        }

        for (Entry<Field, Metadata> e : metadataFields.entrySet()) {
            list.add(createMetaDataMapping(clazz, e.getKey(), e.getValue()));
        }

        return o -> {
            Instant t = timestamp.apply(o);
            return list.stream().map(f -> f.apply(o)).filter(d -> d != null).map(d -> {
                d.timestamp = t;
                return d;
            }).collect(Collectors.toList());
        };
    }

    private static <T extends Annotation> Map<Field, T> getAnnotatedFields(Class<?> clazz, Class<T> annotationType) {
        return Arrays.stream(clazz.getFields()).filter(f -> f.isAnnotationPresent(annotationType))
                .collect(Collectors.toMap(Function.identity(), f -> f.getAnnotation(annotationType)));
    }

    private static Function<Object, Instant> getTimestampMapping(Class<?> clazz) {
        Field timestamp = Arrays.stream(clazz.getFields()).filter(f -> f.isAnnotationPresent(Timestamp.class))
                .findFirst().orElse(null);

        if (timestamp == null) {
            return x -> Instant.now();
        } else {
            ChronoUnit unit = timestamp.getAnnotation(Timestamp.class).value();
            Function<Long, Instant> mapToTimestamp = t -> t == null ? Instant.now() : Instant.EPOCH.plus(t, unit);

            String fieldName = timestamp.getName();
            Function<Object, Long> read = o -> {
                Object t = getValueFromField(fieldName, o);
                if (t == null)
                    return null;
                if (t instanceof String)
                    return Long.parseLong(t.toString());
                if (t instanceof Number)
                    return ((Number) t).longValue();
                throw new IllegalArgumentException("Unable to read timestamp " + t + " from " + fieldName);
            };

            return read.andThen(mapToTimestamp);
        }

    }

    private static Function<Object, DataUpdateDto> createDataMapping(Class<?> clazz, Field f, Data data) {
        String fieldName = f.getName();
        Class<?> type = data.type() == Object.class ? f.getType() : data.type();

        Function<Object, String> model = getModelNameMappingForField(clazz, f);
        Function<Object, String> provider = getProviderNameMappingForField(clazz, f);
        Function<Object, String> service = getServiceNameMappingForField(clazz, f);
        Function<Object, String> resource = getResourceNameMappingForDataField(clazz, f);

        Function<Object, Object> dataValue = o -> getValueFromField(fieldName, o);

        // Do not capture the field or class in this lambda
        Function<Object, DataUpdateDto> dtoMapper = o -> {
            DataUpdateDto dto = new DataUpdateDto();
            dto.data = dataValue.apply(o);

            if (dto.data == null && data.onNull() == NullAction.IGNORE) {
                return null;
            }

            dto.model = model.apply(o);
            dto.provider = provider.apply(o);
            dto.service = service.apply(o);
            dto.resource = resource.apply(o);
            dto.type = type;

            return dto;
        };
        return dtoMapper;
    }

    private static Function<Object, MetadataUpdateDto> createMetaDataMapping(Class<?> clazz, Field f,
            Metadata metadata) {
        String fieldName = f.getName();

        Function<Object, String> model = getModelNameMappingForField(clazz, f);
        Function<Object, String> provider = getProviderNameMappingForField(clazz, f);
        Function<Object, String> service = getServiceNameMappingForField(clazz, f);
        Function<Object, String> resource = getResourceNameMappingForMetadataField(clazz, f);

        Function<Object, Object> metadataValue = o -> getValueFromField(fieldName, o);

        // Do not capture the field or class in this lambda
        Function<Object, MetadataUpdateDto> dtoMapper = o -> {
            MetadataUpdateDto dto = new MetadataUpdateDto();
            Object md = metadataValue.apply(o);

            if (md == null && metadata.onNull() == NullAction.IGNORE) {
                return null;
            }
            String key = NOT_SET.equals(metadata.value()) ? fieldName : metadata.value();

            Map<String, Object> processedMd;
            if (md instanceof Map) {
                processedMd = Collections.singletonMap(key, md);

                for (MapAction ma : metadata.onMap()) {
                    switch (ma) {
                    case REMOVE_MISSING_VALUES:
                        dto.removeMissingValues = true;
                        break;
                    case REMOVE_NULL_VALUES:
                        dto.removeNullValues = true;
                        break;
                    case USE_KEYS_AS_FIELDS:
                        processedMd = ((Map<?, ?>) md).entrySet().stream()
                                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Entry::getValue));
                        break;
                    default:
                        throw new IllegalArgumentException("Unrecognised Map Action " + ma);
                    }
                }

            } else {
                processedMd = Collections.singletonMap(key, md);
            }

            dto.model = model.apply(o);
            dto.provider = provider.apply(o);
            dto.service = service.apply(o);
            dto.resource = resource.apply(o);
            dto.metadata = processedMd;

            return dto;
        };
        return dtoMapper;
    }

    private static Function<Object, String> getModelNameMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Model.class);
        if (mapping == null) {
            // Models are optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getProviderNameMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Provider.class);
        if (mapping == null) {
            throw new IllegalArgumentException("No provider is defined for the field " + f.getName());
        }
        return mapping;
    }

    private static Function<Object, String> getServiceNameMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Service.class);
        if (mapping == null) {
            throw new IllegalArgumentException("No provider is defined for the field " + f.getName());
        }
        return mapping;
    }

    private static Function<Object, String> getResourceNameMappingForDataField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Resource.class);
        if (mapping == null) {
            String fieldName = f.getName();
            mapping = x -> fieldName;
        }
        return mapping;
    }

    /**
     * Separated to avoid capturing the Class
     * 
     * @param fieldName
     * @param update
     * @return
     */
    private static Object getValueFromField(String fieldName, Object update) {
        try {
            return update.getClass().getField(fieldName).get(update);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to read the field %s for the class %s", fieldName, update.getClass()), e);
        }
    }

    private static Function<Object, String> getResourceNameMappingForMetadataField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Resource.class);
        if (mapping == null) {
            throw new IllegalArgumentException("No resource is defined for the field " + f.getName());
        }
        return mapping;
    }

    /**
     * This method must be careful not to capture the class or field in the returned
     * function as it will cause memory leaks if they are referenced in the value of
     * the weak cache
     * 
     * @param clazz
     * @param f
     * @param annotationType
     * @return
     */
    private static Function<Object, String> getAnnotatedNameMapping(Class<?> clazz, Field f,
            Class<? extends Annotation> annotationType) {

        Function<Object, String> mapping = null;

        Method valueMethod = getValueMethod(annotationType);

        // Directly on the field
        if (f.isAnnotationPresent(annotationType)) {
            String value = getAnnotationValue(f, annotationType, valueMethod);
            if (NOT_SET.equals(value))
                throw new IllegalArgumentException(
                        String.format("The class %s has a field %s annotated with %s that has no value",
                                clazz.getName(), f.getName(), annotationType.getSimpleName()));
            mapping = x -> value;
        } else {
            // Check for an annotated field
            Field annotatedField = Arrays.stream(clazz.getFields())
                    .filter(r -> r.isAnnotationPresent(annotationType) && !r.isAnnotationPresent(Data.class))
                    .findFirst().orElse(null);

            if (annotatedField != null) {
                if (annotatedField.getType() != String.class) {
                    throw new IllegalArgumentException(
                            String.format("The class %s has a field %s annotated with %s that has a non String type %s",
                                    clazz.getName(), annotatedField.getName(), annotationType.getSimpleName(),
                                    annotatedField.getType()));
                }
                String fieldName = annotatedField.getName();
                mapping = o -> getTypedValueFromField(fieldName, o, annotationType, String.class);
            } else {
                // Check class level annotation
                if (clazz.isAnnotationPresent(annotationType)) {
                    String value = getAnnotationValue(clazz, annotationType, valueMethod);
                    if (NOT_SET.equals(value))
                        throw new IllegalArgumentException(
                                String.format("The class %s is annotated with %s but that annotation has no value",
                                        clazz.getName(), annotationType.getSimpleName()));
                    mapping = x -> value;
                }
            }
        }
        return mapping;
    }

    private static String getAnnotationValue(AnnotatedElement f, Class<? extends Annotation> annotationType,
            Method valueMethod) {
        String resourceName;
        try {
            resourceName = (String) valueMethod.invoke(f.getAnnotation(annotationType));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("The annotation type %s has no value method", annotationType.getSimpleName()));
        }
        return resourceName;
    }

    private static Method getValueMethod(Class<? extends Annotation> annotationType) {
        Method valueMethod;
        try {
            valueMethod = annotationType.getMethod("value");
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("The annotation type %s has no value method", annotationType.getSimpleName()));
        }
        return valueMethod;
    }

    /**
     * This method must be careful not to capture any types that hold the
     * classloader
     * 
     * @param <T>
     * @param fieldName
     * @param update
     * @param annotationType
     * @param resultType
     * @return
     */
    private static <T> T getTypedValueFromField(String fieldName, Object update,
            Class<? extends Annotation> annotationType, Class<T> resultType) {
        try {
            return resultType.cast(update.getClass().getField(fieldName).get(update));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to read the %s annotated field %s for the class %s",
                            annotationType.getSimpleName(), fieldName, update.getClass()),
                    e);
        }
    }
}
