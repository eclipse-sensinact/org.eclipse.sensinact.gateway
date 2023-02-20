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
package org.eclipse.sensinact.prototype.model.nexus.impl.emf;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

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
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sensinact.model.core.Action;
import org.eclipse.sensinact.model.core.ActionParameter;
import org.eclipse.sensinact.model.core.FeatureCustomMetadata;
import org.eclipse.sensinact.model.core.Metadata;
import org.eclipse.sensinact.model.core.ResourceAttribute;
import org.eclipse.sensinact.model.core.SensiNactFactory;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.model.core.ServiceReference;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.Converters;

/**
 * Some Helper methods to work with Ecores.
 *
 * @author Juergen Albert
 * @since 5 Oct 2022
 */
public class EMFUtil {

    private static final Converter converter;
    private static final Map<Class<?>, EClassifier> typeMap = new HashMap<Class<?>, EClassifier>();
    static {
        converter = Converters.standardConverter();
        EcorePackage.eINSTANCE.getEClassifiers().forEach(ec -> typeMap.put(ec.getInstanceClass(), ec));
        SensiNactPackage.eINSTANCE.getEClassifiers().forEach(ed -> typeMap.put(ed.getInstanceClass(), ed));
    }

    public static Map<String, Object> toEObjectAttributesToMap(EObject eObject) {
        return toEObjectAttributesToMap(eObject, false);
    }

    public static Map<String, Object> toEObjectAttributesToMap(EObject eObject, boolean ignoreUnset) {
        Map<String, Object> result = new HashMap<String, Object>();
        eObject.eClass().getEAllAttributes().stream()
                // We don't want attributes from EObject and anything above
                .filter(ea -> ea.getEContainingClass().getEPackage() != EcorePackage.eINSTANCE)
                .filter(ea -> !ignoreUnset || eObject.eIsSet(ea))
                .forEach(a -> result.put(a.getName(), eObject.eGet(a)));
        return result;
    }

    public static ActionParameter createActionParameter(Entry<String, Class<?>> entry) {
        ActionParameter parameter = SensiNactFactory.eINSTANCE.createActionParameter();
        parameter.setName(entry.getKey());
        parameter.setTimestamp(Instant.now());
        parameter.setEType(convertClass(entry.getValue()));
        return parameter;
    }

    public static EClassifier convertClass(Class<?> clazz) {
        EClassifier eClassifier = typeMap.get(clazz);
        if (eClassifier == null) {
            throw new IllegalArgumentException(
                    "Can't convert " + clazz + " to EClassifier. The class is unknwon to us");
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
        ServiceReference feature = SensiNactFactory.eINSTANCE.createServiceReference();
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
        ResourceAttribute attribute = SensiNactFactory.eINSTANCE.createResourceAttribute();
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
        Object converted;
        if (o == null) {
            converted = o;
        } else {
            EClassifier type = typeMap.get(o.getClass());
            EClassifier target = typeMap.get(targetType);
            if (type == null || target == null) {
                converted = converter.convert(o).to(targetType);
            } else {
                String string = type.getEPackage().getEFactoryInstance().convertToString((EDataType) type, o);
                converted = target.getEPackage().getEFactoryInstance().createFromString((EDataType) target, string);
            }
        }
        return converted;
    }

    public static void fillMetadata(Metadata meta, Instant timestamp, boolean locked, String name,
            Collection<? extends FeatureCustomMetadata> extra) {
        meta.setTimestamp(timestamp);
        meta.setLocked(locked);
        meta.setOriginalName(name);
        meta.getExtra().addAll(extra);

    }

    public static Action createAction(EClass serviceEClass, String name, Class<?> type, List<ActionParameter> params) {
        Action operation = SensiNactFactory.eINSTANCE.createAction();
        operation.setName(name);
        operation.setEType(convertClass(type));
        operation.getEParameters().addAll(params);
        serviceEClass.getEOperations().add(operation);
        return operation;
    }
}
