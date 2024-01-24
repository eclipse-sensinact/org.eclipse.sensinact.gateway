/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
*   Kentyou - fixes and updates to include a basic sensiNact provider
**********************************************************************/
package org.eclipse.sensinact.core.model.nexus.emf;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sensinact.model.core.metadata.Action;
import org.eclipse.sensinact.model.core.metadata.ActionParameter;
import org.eclipse.sensinact.model.core.metadata.MetadataFactory;
import org.eclipse.sensinact.model.core.metadata.MetadataPackage;
import org.eclipse.sensinact.model.core.metadata.NexusMetadata;
import org.eclipse.sensinact.model.core.metadata.ResourceAttribute;
import org.eclipse.sensinact.model.core.metadata.ServiceReference;
import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.osgi.util.converter.ConversionException;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterFunction;
import org.osgi.util.converter.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Some Helper methods to work with Ecores.
 *
 * @author Juergen Albert
 * @since 5 Oct 2022
 */
public class EMFUtil {

    private static final Logger LOG = LoggerFactory.getLogger(EMFUtil.class);

    private static final Converter converter;
    private static final ObjectMapper mapper = JsonMapper.builder().build();
    private static final Map<Class<?>, EClassifier> typeMap = new HashMap<Class<?>, EClassifier>();
    static {
        converter = Converters.newConverterBuilder().errorHandler(EMFUtil::fallbackConversion).build();
        EcorePackage.eINSTANCE.getEClassifiers().forEach(ec -> typeMap.put(ec.getInstanceClass(), ec));
        ProviderPackage.eINSTANCE.getEClassifiers().forEach(ed -> typeMap.put(ed.getInstanceClass(), ed));
        MetadataPackage.eINSTANCE.getEClassifiers().forEach(ed -> typeMap.put(ed.getInstanceClass(), ed));
    }

    // TODO: what to use as good default?
    public static final String BASE_PACKAGE_URI = "https://eclipse.org/sensinact/";
    public static final String DEFAULT_SENSINACT_PACKAGE_URI = BASE_PACKAGE_URI + "default";

    private static Object fallbackConversion(Object o, Type t) {
        try {
            // Avoid infinite recursion when the input is a String
            return o instanceof String ? ConverterFunction.CANNOT_HANDLE : converter.convert(o.toString()).to(t);
        } catch (Exception e) {
            return ConverterFunction.CANNOT_HANDLE;
        }
    }

    public static Map<String, Object> toMetadataAttributesToMap(Metadata metadata, ETypedElement attribute) {
        Map<String, Object> attributes = new HashMap<>(toEObjectAttributesToMap(metadata, false,
                MetadataPackage.Literals.NEXUS_METADATA.getEStructuralFeatures(), null, null));
        for (FeatureCustomMetadata entry : metadata.getExtra()) {
            attributes.put(entry.getName(), entry.getValue());
        }
        EAnnotation metadataAnnotation = attribute.getEAnnotation("Metadata");
        if (metadataAnnotation == null) {
            metadataAnnotation = attribute.getEAnnotation("metadata");
        }
        if (metadataAnnotation != null) {
            metadataAnnotation.getDetails().stream().filter(fcm -> !attributes.containsKey(fcm.getKey()))
                    .forEach(fcm -> attributes.put(fcm.getKey(), fcm.getValue()));
        }
        return attributes;
    }

    public static Map<String, Object> toEObjectAttributesToMap(EObject eObject) {
        return toEObjectAttributesToMap(eObject, false);
    }

    public static Map<String, Object> toEObjectAttributesToMap(EObject eObject, boolean ignoreUnset) {
        return toEObjectAttributesToMap(eObject, ignoreUnset, Collections.emptyList(), null, null);
    }

    public static Map<String, Object> toEObjectAttributesToMap(EObject eObject, boolean ignoreUnset,
            List<EStructuralFeature> ignoreList, EStructuralFeature replacementFeature, Object replacementValue) {
        Map<String, Object> result = new HashMap<String, Object>();
        streamAttributes(eObject.eClass()).filter(Predicate.not(ignoreList::contains))
                .filter(ea -> !ignoreUnset || eObject.eIsSet(ea))
                .forEach(a -> result.put(a.getName(), eObject.eGet(a)));
        if (replacementFeature != null) {
            result.put(replacementFeature.getName(), replacementValue);
        }
        return result;
    }

    /**
     * Provides a {@link Stream} of all {@link EAttribute}s, filtering out all
     * Attributes that are part of {@link EObject} and above
     *
     * @param eClass the {@link EClass} to get Attributes from
     */
    public static Stream<EAttribute> streamAttributes(EClass eClass) {
        return eClass.getEAllAttributes().stream()
                // We don't want attributes from EObject and anything above
                .filter(ea -> ea.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE);
    }

    public static ActionParameter createActionParameter(Entry<String, Class<?>> entry) {
        ActionParameter parameter = MetadataFactory.eINSTANCE.createActionParameter();
        parameter.setName(entry.getKey());
        parameter.setTimestamp(Instant.now());
        parameter.setEType(convertClass(entry.getValue()));
        return parameter;
    }

    private static EClassifier convertClass(Class<?> clazz) {
        EClassifier eClassifier = typeMap.get(clazz);
        if (eClassifier == null) {
            LOG.warn("The class {} has no matching EClassifier. Creating an anonymous EDataType", clazz);
            EDataType dataType = EcorePackage.eINSTANCE.getEcoreFactory().createEDataType();
            dataType.setInstanceClass(clazz);
            dataType.setName("Anonymous EDataType for class " + clazz.getName());
            eClassifier = dataType;
        }
        return eClassifier;
    }

    /**
     * @param name   the name of the {@link EAttribute}
     * @param eClass the {@link EClass} to add the Attribute to
     * @param type   the data type of the attribute
     */
    public static void addAttribute(String name, EClassifier type, EClass eClass) {
        EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
        attribute.setName(name);
        attribute.setEType(type);
        eClass.getEAttributes().add(attribute);
    }

    /**
     * @param name      the name of the EClass
     * @param superType the super type {@link EClass} to use
     */
    public static EClass createEClass(String name, EPackage ePackage,
            Function<EClass, List<EAnnotation>> annotationCreator, EClass... superTypes) {
        EClass eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName(name);
        eClass.getESuperTypes().addAll(Arrays.asList(superTypes));
        ePackage.getEClassifiers().add(eClass);
        if (annotationCreator != null) {
            eClass.getEAnnotations().addAll(annotationCreator.apply(eClass));
        }
        return eClass;
    }

    public static EPackage createPackage(String name, String nsUri, String prefix, ResourceSet resourceSet) {
        Resource resource = new XMIResourceImpl(URI.createURI(nsUri));
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        resource.getContents().add(ePackage);
        ePackage.setName(name);
        ePackage.setNsPrefix(prefix);
        ePackage.setNsURI(nsUri);
        ePackage.setEFactoryInstance(new EFactoryImpl() {
            @Override
            protected EObject basicCreate(EClass eClass) {
                return eClass.getInstanceClassName() == "java.util.Map$Entry"
                        ? new MinimalEObjectImpl.Container.Dynamic.BasicEMapEntry<String, String>(eClass)
                        : new MinimalEObjectImpl.Container.Dynamic.Permissive(eClass);
            }
        });
        resourceSet.getPackageRegistry().put(nsUri, ePackage);
        return ePackage;
    }

    public static EAnnotation createEAnnotation(String source, Map<String, String> detailKeys) {
        EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
        annotation.setSource(source);
        annotation.getDetails().putAll(detailKeys);
        return annotation;
    }

    public static EAnnotation createEAnnotation(String source, List<EObject> content) {
        EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
        annotation.setSource(source);
        annotation.getContents().addAll(content);
        return annotation;
    }

    public static EReference createEReference(EClass parent, String refName, EClass type, boolean containment,
            Function<EStructuralFeature, List<EAnnotation>> annotationCreator) {
        EReference feature = EcoreFactory.eINSTANCE.createEReference();
        feature.setName(refName);
        feature.setEType(type);
        feature.setContainment(containment);
        parent.getEStructuralFeatures().add(feature);
        if (annotationCreator != null) {
            List<EAnnotation> annotations = annotationCreator.apply(feature);
            feature.getEAnnotations().addAll(annotations);
        }
        return feature;
    }

    public static ServiceReference createServiceReference(EClass parent, String refName, EClass type,
            boolean containment) {
        ServiceReference feature = MetadataFactory.eINSTANCE.createServiceReference();
        feature.setName(refName);
        feature.setEType(type);
        feature.setContainment(containment);
        parent.getEStructuralFeatures().add(feature);

        return feature;
    }

    public static EAttribute createEAttribute(EClass service, String resource, Class<?> type, Object defaultValue,
            Function<EStructuralFeature, List<EAnnotation>> annotationCreator) {
        EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
        attribute.setName(resource);
        attribute.setEType(convertClass(type));
        if (defaultValue != null) {
            attribute.setDefaultValue(defaultValue);
        }
        service.getEStructuralFeatures().add(attribute);
        if (annotationCreator != null) {
            attribute.getEAnnotations().addAll(annotationCreator.apply(attribute));
        }
        return attribute;
    }

    public static ResourceAttribute createResourceAttribute(EClass service, String resource, Class<?> type,
            Object defaultValue) {
        ResourceAttribute attribute = MetadataFactory.eINSTANCE.createResourceAttribute();
        attribute.setName(resource);
        attribute.setEType(convertClass(type));
        if (defaultValue != null) {
            attribute.setDefaultValue(defaultValue);
        }
        service.getEStructuralFeatures().add(attribute);
        return attribute;
    }

    public static Object convertToTargetType(EClassifier targetType, Object o) {
        return convertToTargetType(targetType.getInstanceClass(), o);
    }

    public static Object convertToTargetType(Class<?> targetType, Object o) {
        return convertToTargetType((EDataType) typeMap.get(targetType), targetType, o);
    }

    private static Object convertToTargetType(EDataType targetEType, Class<?> targetType, Object o) {
        Object converted;
        if (o == null) {
            converted = o;
        } else {
            EClassifier type = typeMap.get(o.getClass());
            if (type == null || targetEType == null) {
                try {
                    converted = converter.convert(o).to(targetType);
                } catch (ConversionException ce) {
                    if (targetEType != null) {
                        try {
                            converted = convertToTargetType(targetEType, targetType, mapper.writeValueAsString(o));
                        } catch (Exception e) {
                            throw ce;
                        }
                    } else {
                        throw ce;
                    }
                }
            } else {
                String string = type.getEPackage().getEFactoryInstance().convertToString((EDataType) type, o);
                converted = targetEType.getEPackage().getEFactoryInstance().createFromString(targetEType, string);
            }
        }
        return converted;
    }

    public static void fillMetadata(NexusMetadata meta, Instant timestamp, boolean locked, String name,
            Collection<? extends FeatureCustomMetadata> extra) {
        meta.setTimestamp(timestamp);
        meta.setLocked(locked);
        meta.setOriginalName(name);
        meta.getExtra().addAll(extra);

    }

    public static Action createAction(EClass serviceEClass, String name, Class<?> type, List<ActionParameter> params) {
        Action operation = MetadataFactory.eINSTANCE.createAction();
        operation.setName(name);
        operation.setEType(convertClass(type));
        operation.getEParameters().addAll(params);
        serviceEClass.getEOperations().add(operation);
        return operation;
    }

    /**
     * Checks if an Attribute is Marked to have a special Provider name. If not it
     * uses the ID Attribute
     *
     * @param eObject the {@link EObject} to check
     * @return the providerName or null
     */
    public static String getProviderName(EObject eObject) {
        return eObject.eClass().getEAllAttributes().stream().filter(ea -> ea.getEAnnotation("ProviderName") != null)
                .findFirst()
                .map(ea -> !eObject.eIsSet(ea) ? null
                        : EcoreUtil.convertToString(ea.getEAttributeType(), eObject.eGet(ea)))
                .orElseGet(() -> EcoreUtil.getID(eObject));
    }

    public static String getModelName(EClass model) {
        EAnnotation modelAnnotation = model.getEAnnotation("model");
        if (modelAnnotation != null) {
            return model.getEAnnotation("model").getDetails().get("name");
        } else {
            return model.getName();
        }
    }

    /**
     * TODO: we have to sanatize the name, so it fits the conventions of EMF
     *
     * @param modelName the modelname to construct a URI from
     * @return the constructed uri of the package, using the given basepackage
     */
    public static String constructPackageUri(String baseUri, String modelName) {
        StringBuilder uri = new StringBuilder(baseUri);
        if (!baseUri.endsWith("/")) {
            uri.append("/");
        }
        return uri.append(modelName).toString();
    }

    /**
     * @param modelName
     * @return
     */
    public static String constructPackageUri(String modelName) {
        return constructPackageUri(BASE_PACKAGE_URI, modelName);
    }

    /**
     * @param model
     * @return
     */
    public static String constructPackageUri(EClass eClass) {
        return constructPackageUri(BASE_PACKAGE_URI, getModelName(eClass));
    }
}
