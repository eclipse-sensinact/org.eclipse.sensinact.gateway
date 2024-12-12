/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.extract.impl;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.eclipse.sensinact.core.annotation.dto.AnnotationConstants.NOT_SET;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.MapAction;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.ModelPackageUri;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.core.dto.impl.AbstractUpdateDto;
import org.eclipse.sensinact.core.dto.impl.DataUpdateDto;
import org.eclipse.sensinact.core.dto.impl.FailedMappingDto;
import org.eclipse.sensinact.core.dto.impl.MetadataUpdateDto;

public class AnnotationMapping {

    static Function<Object, List<? extends AbstractUpdateDto>> getUpdateDtoMappings(Class<?> clazz) {
        try {
            Map<Field, Data> dataFields = getAnnotatedFields(clazz, Data.class);
            Map<Field, Metadata> metadataFields = getAnnotatedFields(clazz, Metadata.class);

            Function<Object, Instant> timestamp = getTimestampMapping(clazz);

            List<Function<Object, ? extends AbstractUpdateDto>> list = new ArrayList<>();

            for (Entry<Field, Data> e : dataFields.entrySet()) {
                list.add(createDataMapping(clazz, e.getKey(), e.getValue()));
            }

            // Include metadata updates second so that any new resources are created first
            for (Entry<Field, Metadata> e : metadataFields.entrySet()) {
                list.add(createMetaDataMapping(clazz, e.getKey(), e.getValue()));
            }

            return o -> {
                Instant t = null;
                Throwable tFail = null;
                try {
                    t = timestamp.apply(o);
                } catch (Throwable thr) {
                    tFail = thr;
                }
                Instant fT = t;
                Throwable fTFail = tFail;

                return list.stream().map(f -> f.apply(o)).filter(d -> d != null).map(d -> {
                    d.originalDto = o;
                    if (d instanceof FailedMappingDto) {
                        return d;
                    } else if (fTFail != null) {
                        FailedMappingDto fmd = getFailureDto(d);
                        fmd.mappingFailure = fTFail;
                        return fmd;
                    } else {
                        d.timestamp = fT;
                        return d;
                    }
                }).collect(Collectors.toList());
            };
        } catch (Throwable t) {
            Throwable fail = new IllegalArgumentException(
                    String.format("The mapping for class %s is not properly defined", clazz.getName()), t);
            return o -> {
                FailedMappingDto fmd = new FailedMappingDto();
                fmd.originalDto = o;
                fmd.mappingFailure = fail;
                return List.of(fmd);
            };
        }
    }

    private static FailedMappingDto getFailureDto(AbstractUpdateDto dto) {
        FailedMappingDto fmd = new FailedMappingDto();
        fmd.modelPackageUri = dto.modelPackageUri;
        fmd.model = dto.model;
        fmd.provider = dto.provider;
        fmd.service = dto.service;
        fmd.resource = dto.resource;
        fmd.timestamp = dto.timestamp;
        fmd.originalDto = dto.originalDto;
        return fmd;
    }

    private static FailedMappingDto getFailureDto(AbstractUpdateDto dto, Throwable cause) {
        FailedMappingDto fmd = getFailureDto(dto);
        fmd.mappingFailure = cause;
        return fmd;
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
            Function<Object, Instant> read = o -> {
                Object t = getValueFromField(fieldName, o);
                if (t == null)
                    return null;
                if (t instanceof String)
                    try {
                        return mapToTimestamp.apply(Long.valueOf(t.toString()));
                    } catch (NumberFormatException nfe) {
                        return Instant.from(ISO_DATE_TIME.parse(t.toString()));
                    }
                if (t instanceof Number) {
                    long l = ((Number) t).longValue();
                    return mapToTimestamp.apply(l);
                }
                if (t instanceof Temporal)
                    return mapToTimestamp.apply(unit.between(Instant.EPOCH, (Temporal) t));
                throw new IllegalArgumentException("Unable to read timestamp " + t + " from " + fieldName);
            };

            return read;
        }

    }

    private static Function<Object, ? extends AbstractUpdateDto> createDataMapping(Class<?> clazz, Field f, Data data) {
        String fieldName = f.getName();
        Class<?> type = data.type() == Object.class ? f.getType() : data.type();

        Function<Object, String> modelPackageUri = getModelPackageUriMappingForField(clazz, f);
        Function<Object, String> model = getModelNameMappingForField(clazz, f);
        Function<Object, EClass> modelEClass = getModelEClassMappingForField(clazz, f);
        Function<Object, String> provider = getProviderNameMappingForField(clazz, f);
        Function<Object, String> service = getServiceNameMappingForField(clazz, f);
        Function<Object, EClass> serviceEClass = getServiceEClassMappingForField(clazz, f);
        Function<Object, EReference> serviceReference = getServiceEReferenceMappingForField(clazz, f);
        Function<Object, String> resource = getResourceNameMappingForDataField(clazz, f);

        Function<Object, Object> dataValue = o -> getValueFromField(fieldName, o);

        // Do not capture the field or class in this lambda
        Function<Object, ? extends AbstractUpdateDto> dtoMapper = o -> {
            DataUpdateDto dto = new DataUpdateDto();
            Throwable firstFailure = null;

            try {
                dto.data = dataValue.apply(o);
            } catch (Throwable t) {
                firstFailure = t;
            }

            if (dto.data == null && data.onNull() == NullAction.IGNORE && firstFailure == null) {
                return null;
            } else {
                dto.actionOnNull = data.onNull();
            }

            dto.actionOnDuplicate = data.onDuplicate();

            try {
                dto.modelPackageUri = modelPackageUri.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.model = model.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.modelEClass = modelEClass.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.provider = provider.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.serviceEClass = serviceEClass.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.serviceReference = serviceReference.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.service = service.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.resource = resource.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            dto.type = type;

            if (dto.service == null) {
                if (dto.serviceReference != null) {
                    dto.service = dto.serviceReference.getName();
                } else {
                    firstFailure = firstFailure == null ? new IllegalArgumentException(
                            String.format("No service or service EReference is defined for the field %s in class %s",
                                    f.getName(), clazz.getName()))
                            : firstFailure;
                }
            }
            if (dto.service != null && dto.serviceReference != null
                    && !dto.serviceReference.getName().equals(dto.service)) {
                firstFailure = firstFailure == null ? new IllegalArgumentException(String.format(
                        "The defined service name %s does not match the defined EReference %s for the field %s in class %s",
                        dto.service, dto.serviceReference.getName(), f.getName(), clazz.getName())) : firstFailure;
            }
            if (dto.serviceReference != null && dto.serviceEClass != null
                    && !dto.serviceReference.getEReferenceType().isSuperTypeOf(dto.serviceEClass)) {
                firstFailure = firstFailure == null ? new IllegalArgumentException(String.format(
                        "The defined service EClass %s is no supertype EReferences return type %s for the field %s in class %s",
                        dto.serviceEClass.getName(), dto.serviceReference.getEReferenceType().getName(), f.getName(),
                        clazz.getName())) : firstFailure;
            }

            if (firstFailure != null) {
                return getFailureDto(dto, firstFailure);
            }

            return dto;
        };
        return dtoMapper;
    }

    private static Function<Object, ? extends AbstractUpdateDto> createMetaDataMapping(Class<?> clazz, Field f,
            Metadata metadata) {
        String fieldName = f.getName();

        Function<Object, String> modelPackageUri = getModelPackageUriMappingForField(clazz, f);
        Function<Object, String> model = getModelNameMappingForField(clazz, f);
        Function<Object, EClass> modelEClass = getModelEClassMappingForField(clazz, f);
        Function<Object, String> provider = getProviderNameMappingForField(clazz, f);
        Function<Object, String> service = getServiceNameMappingForField(clazz, f);
        Function<Object, EClass> serviceEClass = getServiceEClassMappingForField(clazz, f);
        Function<Object, EReference> serviceReference = getServiceEReferenceMappingForField(clazz, f);
        Function<Object, String> resource = getResourceNameMappingForMetadataField(clazz, f);

        Function<Object, Object> metadataValue = o -> getValueFromField(fieldName, o);

        // Do not capture the field or class in this lambda
        Function<Object, ? extends AbstractUpdateDto> dtoMapper = o -> {
            MetadataUpdateDto dto = new MetadataUpdateDto();

            Throwable firstFailure = null;

            Object md = null;
            try {
                md = metadataValue.apply(o);
            } catch (Throwable t) {
                firstFailure = t;
            }

            if (md == null && metadata.onNull() == NullAction.IGNORE && firstFailure == null) {
                return null;
            } else {
                dto.actionOnNull = metadata.onNull();
            }

            dto.actionOnDuplicate = metadata.onDuplicate();

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
                        firstFailure = new IllegalArgumentException("Unrecognised Map Action " + ma + " for field "
                                + f.getName() + " in class " + clazz.getName());
                    }
                }

            } else {
                processedMd = Collections.singletonMap(key, md);
            }
            try {
                dto.modelPackageUri = modelPackageUri.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.model = model.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.modelEClass = modelEClass.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.provider = provider.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.serviceEClass = serviceEClass.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.serviceReference = serviceReference.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.service = service.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            try {
                dto.resource = resource.apply(o);
            } catch (Throwable t) {
                firstFailure = firstFailure == null ? t : firstFailure;
            }
            dto.metadata = processedMd;

            if (firstFailure != null) {
                return getFailureDto(dto, firstFailure);
            }

            return dto;
        };
        return dtoMapper;
    }

    private static Function<Object, String> getModelPackageUriMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, ModelPackageUri.class, String.class);
        if (mapping == null) {
            // Models are optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getModelNameMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Model.class, String.class);
        if (mapping == null) {
            // Models are optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, EClass> getModelEClassMappingForField(Class<?> clazz, Field f) {
        Function<Object, EClass> mapping = getAnnotatedNameMapping(clazz, f, Model.class, EClass.class);
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getProviderNameMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Provider.class, String.class);
        if (mapping == null) {
            throw new IllegalArgumentException(
                    String.format("No provider is defined for the field %s in class %s", f.getName(), clazz.getName()));
        }
        return mapping;
    }

    private static Function<Object, String> getServiceNameMappingForField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Service.class, String.class);
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, EClass> getServiceEClassMappingForField(Class<?> clazz, Field f) {
        Function<Object, EClass> mapping = getAnnotatedNameMapping(clazz, f, Service.class, EClass.class);
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, EReference> getServiceEReferenceMappingForField(Class<?> clazz, Field f) {
        Function<Object, EReference> mapping = getAnnotatedNameMapping(clazz, f, Service.class, EReference.class);
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getResourceNameMappingForDataField(Class<?> clazz, Field f) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Resource.class, String.class);
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
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, f, Resource.class, String.class);
        if (mapping == null) {
            throw new IllegalArgumentException(
                    String.format("No resource is defined for the field %s in class %s", f.getName(), clazz.getName()));
        }
        return mapping;
    }

    /**
     * This method must be careful not to capture the class or field in the returned
     * function as it will cause memory leaks if they are referenced in the value of
     * the weak cache
     *
     * @param <T>
     *
     * @param clazz
     * @param f
     * @param annotationType
     * @return
     */
    private static <T> Function<Object, T> getAnnotatedNameMapping(Class<?> clazz, Field f,
            Class<? extends Annotation> annotationType, Class<T> resultType) {

        Function<Object, T> mapping = null;

        Method valueMethod = getValueMethod(annotationType);

        // Directly on the field and only in the case a String is required
        if (f.isAnnotationPresent(annotationType) && resultType == String.class) {
            @SuppressWarnings("unchecked")
            T value = (T) getAnnotationValue(f, annotationType, valueMethod);
            if (NOT_SET.equals(value))
                throw new IllegalArgumentException(
                        String.format("The class %s has a field %s annotated with %s that has no value",
                                clazz.getName(), f.getName(), annotationType.getSimpleName()));
            mapping = x -> value;
        } else {
            // Check for an annotated field
            Field annotatedField = Arrays.stream(clazz.getFields())
                    .filter(r -> r.isAnnotationPresent(annotationType) && !r.isAnnotationPresent(Data.class)
                            && !r.isAnnotationPresent(Metadata.class))
                    .filter(r -> resultType == r.getType()).findFirst().orElse(null);

            if (annotatedField != null) {
                if (annotatedField.getType() != resultType) {
//                    throw new IllegalArgumentException(
//                            String.format("The class %s has a field %s annotated with %s that has a non String type %s",
//                                    clazz.getName(), annotatedField.getName(), annotationType.getSimpleName(),
//                                    annotatedField.getType()));
                    return null;
                }
                String fieldName = annotatedField.getName();
                mapping = o -> getTypedValueFromField(fieldName, o, annotationType, resultType);
            } else if (resultType == String.class) {
                // Check class level annotation
                if (clazz.isAnnotationPresent(annotationType)) {
                    @SuppressWarnings("unchecked")
                    T value = (T) getAnnotationValue(clazz, annotationType, valueMethod);
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
