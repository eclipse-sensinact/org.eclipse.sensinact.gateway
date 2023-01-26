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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.sensinact.model.core.ModelMetadata;
import org.eclipse.sensinact.model.core.SensiNactPackage;

/**
 * Some Helper methods to work with Ecores.
 *
 * @author Juergen Albert
 * @since 5 Oct 2022
 */
public class EMFUtil {

    private static final Map<Class<?>, EClassifier> typeMap = new HashMap<Class<?>, EClassifier>();
    static {
        EcorePackage.eINSTANCE.getEClassifiers().forEach(ec -> typeMap.put(ec.getInstanceClass(), ec));
        EDataType eInstant = SensiNactPackage.eINSTANCE.getEInstant();
        typeMap.put(eInstant.getInstanceClass(), eInstant);
        EDataType eGeoJsonObject = SensiNactPackage.eINSTANCE.getEGeoJsonObject();
        typeMap.put(eGeoJsonObject.getInstanceClass(), eGeoJsonObject);
    }

    public static Map<String, Object> toEObjectAttributesToMap(EObject eObject) {
        Map<String, Object> result = new HashMap<String, Object>();
        eObject.eClass().getEAttributes().forEach(a -> result.put(a.getName(), eObject.eGet(a)));
        return result;
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
        resourceSet.getResources().add(resource);
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        resource.getContents().add(ePackage);
        ePackage.setName(name);
        ePackage.setNsPrefix(prefix);
        ePackage.setNsURI(nsUri);
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

    /**
     * @param serviceName
     * @param service
     * @param b
     * @return
     */
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

    /**
     * TODO: What should we do if the type is unkwon?
     *
     * @param service
     * @param resource
     * @param type
     * @return
     */
    public static EAttribute createEAttribute(EClass service, String resource, Class<?> type,
            Function<EStructuralFeature, List<EAnnotation>> annotationCreator) {
        EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
        attribute.setName(resource);
        attribute.setEType(typeMap.get(type));
        service.getEStructuralFeatures().add(attribute);
        if (annotationCreator != null) {
            attribute.getEAnnotations().addAll(annotationCreator.apply(attribute));
        }
        return attribute;
    }

    /**
     * @param feature
     * @return
     */
    public static int getVersion(EModelElement eModelElement) {
        EAnnotation eAnnotation = eModelElement.getEAnnotation("metadata");
        if (eAnnotation == null) {
            return -1;
        }
        return ((ModelMetadata) eAnnotation.getContents().get(0)).getVersion();
    }

    /**
     * Returns the Version of the containing {@link EClass} of the
     * {@link EStructuralFeature}
     *
     * @param feature the Feature
     * @return the version of the container
     */
    public static int getContainerVersion(EStructuralFeature feature) {
        return getVersion((EClass) feature.eContainer());
    }

    public static Object convertToTargetType(EClassifier targetType, Object o) {

        if (o == null) {
            return targetType.getInstanceClass().isPrimitive() ? targetType.getDefaultValue() : o;
        } else {
            EClassifier type = typeMap.get(o.getClass());
            if (type == null) {
                throw new IllegalArgumentException("Unknown data type " + o.getClass());
            }
            String string = type.getEPackage().getEFactoryInstance().convertToString((EDataType) type, o);
            return targetType.getEPackage().getEFactoryInstance().createFromString((EDataType) targetType, string);
        }
    }
}
