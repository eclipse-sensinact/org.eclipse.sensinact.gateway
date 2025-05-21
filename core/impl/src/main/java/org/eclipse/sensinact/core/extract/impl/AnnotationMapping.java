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
*   Kentyou - initial implementation, update to support Records
**********************************************************************/
package org.eclipse.sensinact.core.extract.impl;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.eclipse.sensinact.core.annotation.dto.AnnotationConstants.NOT_SET;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
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

    private static enum ElementType {
        FIELDS {
            @Override
            Field[] elements(Class<?> clazz) {
                return clazz.getFields();
            }

            @Override
            String getName(AnnotatedElement element) {
                return ((Field) element).getName();
            }

            @Override
            Class<?> getType(AnnotatedElement element) {
                return ((Field) element).getType();
            }

            @Override
            Object getElementValue(Object source, String elementName) throws Exception {
                return Arrays.stream(elements(source.getClass()))
                        .filter(f -> elementName.equals(f.getName()))
                        .findFirst().get().get(source);
            }
        }, RECORD_COMPONENTS {
            @Override
            RecordComponent[] elements(Class<?> clazz) {
                return clazz.getRecordComponents();
            }

            @Override
            String getName(AnnotatedElement element) {
                return ((RecordComponent) element).getName();
            }

            @Override
            Class<?> getType(AnnotatedElement element) {
                return ((RecordComponent) element).getType();
            }

            @Override
            Object getElementValue(Object source, String elementName) throws Exception {
                return Arrays.stream(elements(source.getClass()))
                        .filter(rc -> elementName.equals(rc.getName()))
                        .findFirst().get().getAccessor().invoke(source);
            }
        };

        abstract AnnotatedElement[] elements(Class<?> clazz);

        abstract String getName(AnnotatedElement element);

        abstract Class<?> getType(AnnotatedElement element);

        abstract Object getElementValue(Object source, String elementName) throws Exception;
    }

    static Function<Object, List<? extends AbstractUpdateDto>> getUpdateDtoMappings(Class<?> clazz) {
        try {

            ElementType elementType = Record.class.isAssignableFrom(clazz) ? ElementType.RECORD_COMPONENTS : ElementType.FIELDS;

            Map<AnnotatedElement, Data> dataFields = getAnnotatedElements(clazz, elementType, Data.class);
            Map<AnnotatedElement, Metadata> metadataFields = getAnnotatedElements(clazz, elementType, Metadata.class);

            Function<Object, Instant> timestamp = getTimestampMapping(clazz, elementType);

            List<Function<Object, ? extends AbstractUpdateDto>> list = new ArrayList<>();

            for (Entry<AnnotatedElement, Data> e : dataFields.entrySet()) {
                list.add(createDataMapping(clazz, elementType, e.getKey(), e.getValue()));
            }

            // Include metadata updates second so that any new resources are created first
            for (Entry<AnnotatedElement, Metadata> e : metadataFields.entrySet()) {
                list.add(createMetaDataMapping(clazz, elementType, e.getKey(), e.getValue()));
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

    private static <T extends Annotation> Map<AnnotatedElement, T> getAnnotatedElements(Class<?> clazz,
            ElementType elementType, Class<T> annotationType) {
        return Arrays.stream(elementType.elements(clazz)).filter(f -> f.isAnnotationPresent(annotationType))
                .collect(Collectors.toMap(Function.identity(), f -> f.getAnnotation(annotationType)));
    }

    private static Function<Object, Instant> getTimestampMapping(Class<?> clazz, ElementType elementType) {

        AnnotatedElement timestamp = Arrays.stream(elementType.elements(clazz)).
                filter(f -> f.isAnnotationPresent(Timestamp.class))
                .findFirst().orElse(null);

        if (timestamp == null) {
            return x -> Instant.now();
        } else {
            ChronoUnit unit = timestamp.getAnnotation(Timestamp.class).value();
            Function<Long, Instant> mapToTimestamp = t -> t == null ? Instant.now() : Instant.EPOCH.plus(t, unit);

            String name = elementType.getName(timestamp);
            Function<Object, Instant> read = o -> {
                Object t = getValueFromElement(elementType, name, o);
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
                throw new IllegalArgumentException("Unable to read timestamp " + t + " from " + name);
            };

            return read;
        }

    }

    private static Function<Object, ? extends AbstractUpdateDto> createDataMapping(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae, Data data) {
        String name = elementType.getName(ae);
        Class<?> type = data.type() == Object.class ? elementType.getType(ae) : data.type();

        Function<Object, String> modelPackageUri = getModelPackageUriMappingForElement(clazz, elementType, ae);
        Function<Object, String> model = getModelNameMappingForElement(clazz, elementType, ae);
        Function<Object, EClass> modelEClass = getModelEClassMappingForElement(clazz, elementType, ae);
        Function<Object, String> provider = getProviderNameMappingForElement(clazz, elementType, ae);
        Function<Object, String> service = getServiceNameMappingForElement(clazz, elementType, ae);
        Function<Object, EClass> serviceEClass = getServiceEClassMappingForElement(clazz, elementType, ae);
        Function<Object, EReference> serviceReference = getServiceEReferenceMappingForElement(clazz, elementType, ae);
        Function<Object, String> resource = getResourceNameMappingForDataElement(clazz, elementType, ae);

        Function<Object, Object> dataValue = o -> getValueFromElement(elementType, name, o);

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
                                    elementType.getName(ae), clazz.getName()))
                            : firstFailure;
                }
            }
            if (dto.service != null && dto.serviceReference != null
                    && !dto.serviceReference.getName().equals(dto.service)) {
                firstFailure = firstFailure == null ? new IllegalArgumentException(String.format(
                        "The defined service name %s does not match the defined EReference %s for the field %s in class %s",
                        dto.service, dto.serviceReference.getName(), elementType.getName(ae), clazz.getName())) : firstFailure;
            }
            if (dto.serviceReference != null && dto.serviceEClass != null
                    && !dto.serviceReference.getEReferenceType().isSuperTypeOf(dto.serviceEClass)) {
                firstFailure = firstFailure == null ? new IllegalArgumentException(String.format(
                        "The defined service EClass %s is no supertype EReferences return type %s for the field %s in class %s",
                        dto.serviceEClass.getName(), dto.serviceReference.getEReferenceType().getName(), elementType.getName(ae),
                        clazz.getName())) : firstFailure;
            }

            if (firstFailure != null) {
                return getFailureDto(dto, firstFailure);
            }

            return dto;
        };
        return dtoMapper;
    }

    private static Function<Object, ? extends AbstractUpdateDto> createMetaDataMapping(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae, Metadata metadata) {
        String name = elementType.getName(ae);

        Function<Object, String> modelPackageUri = getModelPackageUriMappingForElement(clazz, elementType, ae);
        Function<Object, String> model = getModelNameMappingForElement(clazz, elementType, ae);
        Function<Object, EClass> modelEClass = getModelEClassMappingForElement(clazz, elementType, ae);
        Function<Object, String> provider = getProviderNameMappingForElement(clazz, elementType, ae);
        Function<Object, String> service = getServiceNameMappingForElement(clazz, elementType, ae);
        Function<Object, EClass> serviceEClass = getServiceEClassMappingForElement(clazz, elementType, ae);
        Function<Object, EReference> serviceReference = getServiceEReferenceMappingForElement(clazz, elementType, ae);
        Function<Object, String> resource = getResourceNameMappingForMetadataElement(clazz, elementType, ae);

        Function<Object, Object> metadataValue = o -> getValueFromElement(elementType, name, o);

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

            String key = NOT_SET.equals(metadata.value()) ? name : metadata.value();

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
                        Map<?, ?> mdMap = (Map<?, ?>) md;
                        Map<String, Object> tmp = new HashMap<>(mdMap.size() * 2);
                        mdMap.entrySet().stream().forEach(e -> tmp.put(Objects.toString(e.getKey()), e.getValue()));
                        processedMd = Collections.unmodifiableMap(tmp);
                        break;
                    default:
                        firstFailure = new IllegalArgumentException("Unrecognised Map Action " + ma + " for field "
                                + name + " in class " + clazz.getName());
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

    private static Function<Object, String> getModelPackageUriMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, elementType, ae, ModelPackageUri.class,
                String.class, Set.of());
        if (mapping == null) {
            // Models are optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getModelNameMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Model.class, String.class,
                Set.of(EClass.class));
        if (mapping == null) {
            // Models are optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, EClass> getModelEClassMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, EClass> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Model.class, EClass.class,
                Set.of(String.class));
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getProviderNameMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Provider.class,
                String.class, Set.of());
        if (mapping == null) {
            throw new IllegalArgumentException(
                    String.format("No provider is defined for the field %s in class %s", elementType.getName(ae), clazz.getName()));
        }
        return mapping;
    }

    private static Function<Object, String> getServiceNameMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Service.class,
                String.class, Set.of(EClass.class, EReference.class));
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, EClass> getServiceEClassMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, EClass> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Service.class,
                EClass.class, Set.of(String.class, EReference.class));
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, EReference> getServiceEReferenceMappingForElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, EReference> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Service.class,
                EReference.class, Set.of(String.class, EClass.class));
        if (mapping == null) {
            // Models EClass is optional
            mapping = o -> null;
        }
        return mapping;
    }

    private static Function<Object, String> getResourceNameMappingForDataElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Resource.class,
                String.class, Set.of());
        if (mapping == null) {
            String fieldName = elementType.getName(ae);
            mapping = x -> fieldName;
        }
        return mapping;
    }

    /**
     * Separated to avoid capturing the Class
     *
     * @param elementName
     * @param update
     * @return
     */
    private static Object getValueFromElement(ElementType elementType, String elementName, Object update) {
        try {
            return elementType.getElementValue(update, elementName);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to read the element %s for the class %s", elementName, update.getClass()), e);
        }
    }

    private static Function<Object, String> getResourceNameMappingForMetadataElement(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae) {
        Function<Object, String> mapping = getAnnotatedNameMapping(clazz, elementType, ae, Resource.class, String.class,
                Set.of());
        if (mapping == null) {
            throw new IllegalArgumentException(
                    String.format("No resource is defined for the field %s in class %s", elementType.getName(ae), clazz.getName()));
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
    private static <T> Function<Object, T> getAnnotatedNameMapping(Class<?> clazz, ElementType elementType,
            AnnotatedElement ae, Class<? extends Annotation> annotationType, Class<T> resultType,
            Set<Class<?>> otherPossibleTypes) {

        Function<Object, T> mapping = null;

        Method valueMethod = getValueMethod(annotationType);

        // Directly on the field and only in the case a String is required
        if (ae.isAnnotationPresent(annotationType) && resultType == String.class) {
            @SuppressWarnings("unchecked")
            T value = (T) getAnnotationValue(ae, annotationType, valueMethod);
            if (NOT_SET.equals(value))
                throw new IllegalArgumentException(
                        String.format("The class %s has a field %s annotated with %s that has no value",
                                clazz.getName(), elementType.getName(ae), annotationType.getSimpleName()));
            mapping = x -> value;
        } else {
            // Check for an annotated field
            AnnotatedElement annotatedElement = Arrays.stream(elementType.elements(clazz))
                    .filter(r -> r.isAnnotationPresent(annotationType) && !r.isAnnotationPresent(Data.class)
                            && !r.isAnnotationPresent(Metadata.class))
                    // Exclude other legal types that we aren't looking for
                    .filter(r -> !otherPossibleTypes.contains(elementType.getType(r)))
                    .findFirst().orElse(null);

            if (annotatedElement != null) {
                String name = elementType.getName(annotatedElement);
                Class<?> type = elementType.getType(annotatedElement);
                if (type != resultType) {
                    throw new IllegalArgumentException(
                            String.format("The class %s has a field %s annotated with %s that has a non String type %s",
                                    clazz.getName(), name, annotationType.getSimpleName(),
                                    type));
                }
                mapping = o -> getTypedValueFromElement(elementType, name, o, annotationType, resultType);
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

    private static String getAnnotationValue(AnnotatedElement ae, Class<? extends Annotation> annotationType,
            Method valueMethod) {
        String resourceName;
        try {
            resourceName = (String) valueMethod.invoke(ae.getAnnotation(annotationType));
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
    private static <T> T getTypedValueFromElement(ElementType elementType, String elementName, Object update,
            Class<? extends Annotation> annotationType, Class<T> resultType) {
        try {
            return resultType.cast(elementType.getElementValue(update, elementName));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to read the %s annotated element %s for the class %s",
                            annotationType.getSimpleName(), elementName, update.getClass()),
                    e);
        }
    }
}
