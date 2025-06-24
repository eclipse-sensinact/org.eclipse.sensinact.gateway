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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
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
import org.eclipse.sensinact.model.core.provider.ActionMetadata;
import org.eclipse.sensinact.model.core.provider.ActionParameterMetadata;
import org.eclipse.sensinact.model.core.provider.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.provider.Metadata;
import org.eclipse.sensinact.model.core.provider.ModelMetadata;
import org.eclipse.sensinact.model.core.provider.NexusMetadata;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.model.core.provider.ResourceMetadata;
import org.eclipse.sensinact.model.core.provider.ServiceReferenceMetadata;
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

    /** METADATA2 */
    public static final String METADATA_ANNOTATION_SOURCE = "Metadata";

    private static final Logger LOG = LoggerFactory.getLogger(EMFUtil.class);

    private static final Converter converter;
    private static final ObjectMapper mapper = JsonMapper.builder().build();
    private static final Map<Class<?>, EClassifier> typeMap = new HashMap<Class<?>, EClassifier>();
    static {
        converter = Converters.newConverterBuilder().errorHandler(EMFUtil::fallbackConversion).build();
        EcorePackage.eINSTANCE.getEClassifiers().forEach(ec -> typeMap.put(ec.getInstanceClass(), ec));
        ProviderPackage.eINSTANCE.getEClassifiers().forEach(ed -> typeMap.put(ed.getInstanceClass(), ed));
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

    public static Map<String, Object> toMetadataAttributesToMap(ETypedElement attribute) {
        return toMetadataAttributesToMap(null, attribute);
    }

    public static Map<String, Object> toMetadataAttributesToMap(Metadata metadata, EModelElement element) {
        Map<String, Object> attributes = new HashMap<>();
        NexusMetadata nexusMetadata = getModelMetadata(element);
        if (nexusMetadata != null) {
            attributes.put("timestamp", nexusMetadata.getTimestamp());
            nexusMetadata.getExtra().forEach(entry -> attributes.put(entry.getKey(), entry.getValue().getValue()));
        }
        if (metadata != null) {
            attributes.putAll(toEObjectAttributesToMap(metadata, false, List.of(), null, null));
            for (Entry<String, FeatureCustomMetadata> entry : metadata.getExtra()) {
                attributes.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        return attributes;
    }

    /**
     * This method will look if the element has an {@link EAnnotation} with the
     * source {@link EMFUtil#METADATA_ANNOTATION_SOURCE}. If one is found it will
     * first look if the content list contains a {@link NexusMetadata} object. It
     * will also look in the Details, as for convenience we also support setting
     * metadata via simple key value pairs. If it has Details and ModelNexus
     * metadata the Details will be applied to the {@link NexusMetadata} and may
     * overwrite existing attributes. If no {@link NexusMetadata} Object was
     * present, one will be created, that might not be attached to the given
     * ModelElement.
     *
     * @param element the Feature to look
     * @return
     */
    public static NexusMetadata getModelMetadata(EModelElement element) {
        NexusMetadata result = null;
        EAnnotation eAnnotation = element.getEAnnotation(METADATA_ANNOTATION_SOURCE);
        if (eAnnotation != null) {
            result = eAnnotation.getContents().stream().filter(meta -> meta instanceof NexusMetadata)
                    .map(NexusMetadata.class::cast).findFirst().orElseGet(() -> null);
            if (!eAnnotation.getDetails().isEmpty()) {
                if (result == null) {
                    result = createCorrectNexusMetadata(element);
                }
                fillMetadataFromAnnotationDetails(eAnnotation.getDetails(), result);
            }
        }
        return result;
    }

    public static FeatureCustomMetadata createFeatureCustomMetadata(Instant timestamp,
            Object value) {
        return handleFeatureCustomMetadata(ProviderFactory.eINSTANCE.createFeatureCustomMetadata(), timestamp,
                value);
    }

    public static FeatureCustomMetadata handleFeatureCustomMetadata(FeatureCustomMetadata customMetadata,
            Instant timestamp, Object value) {
        customMetadata.setTimestamp(timestamp);
        customMetadata.setValue(value);
        return customMetadata;
    }

    private static void fillMetadataFromAnnotationDetails(EMap<String, String> details, NexusMetadata metadata) {
        for (Entry<String, String> entry : details.entrySet()) {
            EStructuralFeature eStructuralFeature = metadata.eClass().getEStructuralFeature(entry.getKey());
            if (eStructuralFeature instanceof EAttribute attribute) {
                metadata.eSet(eStructuralFeature, EcoreUtil
                        .createFromString(attribute.getEAttributeType(), entry.getValue()));
            } else {
                metadata.getExtra().put(entry.getKey(), createFeatureCustomMetadata(null, entry.getValue()));
            }
        }

    }

    private static NexusMetadata createCorrectNexusMetadata(EModelElement element) {

        if (element instanceof EAttribute) {
            return ProviderFactory.eINSTANCE.createResourceMetadata();
        } else if (element instanceof EReference) {
            return ProviderFactory.eINSTANCE.createServiceReferenceMetadata();
        } else if (element instanceof EOperation) {
            return ProviderFactory.eINSTANCE.createActionMetadata();
        } else if (element instanceof EParameter) {
            return ProviderFactory.eINSTANCE.createActionParameterMetadata();
        } else if (element instanceof EClass) {
            return ProviderFactory.eINSTANCE.createModelMetadata();
        }
        throw new UnsupportedOperationException(
                element.eClass().getName() + " is unsupported and we can't create fitting Metadata for it.");
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

    public static EParameter createActionParameter(Entry<String, Class<?>> entry) {
        ActionParameterMetadata metaData = ProviderFactory.eINSTANCE.createActionParameterMetadata();
        metaData.setTimestamp(Instant.now());
        EParameter parameter = EcoreFactory.eINSTANCE.createEParameter();
        parameter.setName(entry.getKey());
        parameter.setEType(convertClass(entry.getValue()));
        addMetaDataAnnnotation(parameter, metaData);
        return parameter;
    }

    public static void addMetaDataAnnnotation(EModelElement model, EObject metaData) {
        EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
        annotation.getContents().add(metaData);
        annotation.setSource(METADATA_ANNOTATION_SOURCE);
        model.getEAnnotations().add(annotation);
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

    public static EReference createServiceReference(EClass parent, String refName, EClass type, boolean containment) {
        EReference feature = createEReference(parent, refName, type, containment, null);
        ServiceReferenceMetadata metaData = ProviderFactory.eINSTANCE.createServiceReferenceMetadata();
        addMetaDataAnnnotation(feature, metaData);
        return feature;
    }

    public static ResourceMetadata createResourceAttribute(EClass service, String resource, Class<?> type,
            Object defaultValue) {
        ResourceMetadata metaData = ProviderFactory.eINSTANCE.createResourceMetadata();
        EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
        attribute.setName(resource);
        attribute.setEType(convertClass(type));
        if (defaultValue != null) {
            attribute.setDefaultValue(defaultValue);
        }
        service.getEStructuralFeatures().add(attribute);
        addMetaDataAnnnotation(attribute, metaData);
        return metaData;
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
            EMap<String, FeatureCustomMetadata> extra) {
        meta.setTimestamp(timestamp);
        meta.setLocked(locked);
        meta.setOriginalName(name);
        extra.forEach(e -> meta.getExtra().put(e.getKey(), e.getValue()));

    }

    public static EOperation createAction(EClass serviceEClass, String name, Class<?> type, List<EParameter> params) {
        ActionMetadata metaData = ProviderFactory.eINSTANCE.createActionMetadata();
        EOperation operation = EcoreFactory.eINSTANCE.createEOperation();
        operation.setName(name);
        operation.setEType(convertClass(type));
        operation.getEParameters().addAll(params);
        serviceEClass.getEOperations().add(operation);
        addMetaDataAnnnotation(operation, metaData);
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
        ModelMetadata metadata = (ModelMetadata) getModelMetadata(model);
        if (metadata != null) {
            return metadata.getOriginalName();
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
