/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.sensinact.prototype.model.nexus.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

/**
 * Some Helper methods to work with Ecores.
 * @author Juergen Albert
 * @since 5 Oct 2022
 */
public class EMFUtil {

	private static final Map<Class<?>, EClassifier> typeMap = new HashMap<Class<?>, EClassifier>();
	static {
		EcorePackage.eINSTANCE.getEClassifiers().forEach(ec -> typeMap.put(ec.getInstanceClass(), ec));
	}
	
	/**
	 * @param name the name of the {@link EAttribute}
	 * @param eClass the {@link EClass} to add the Attribute to
	 * @param type the data type of the attribute
	 */
	public static void addAttribute(String name, EClassifier type, EClass eClass) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		eClass.getEAttributes().add(attribute);
	}

	/**
	 * @param name the name of the EClass
	 * @param superType the super type {@link EClass} to use
	 */
	public static EClass createEClass(String name, EPackage ePackage, EClass... superTypes ) {
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(name);
		eClass.getEAllSuperTypes().addAll(Arrays.asList(superTypes));
		ePackage.getEClassifiers().add(eClass);
		return eClass;
	}
	
	public static EPackage createPackage(String name, String nsUri, String prefix, ResourceSet resourceSet) {
		Resource resource = resourceSet.createResource(URI.createURI(nsUri));
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		resource.getContents().add(ePackage);
		ePackage.setName(name);
		ePackage.setNsPrefix(prefix);
		ePackage.setNsURI(nsUri);
		return ePackage;
	}

	/**
	 * @param serviceName
	 * @param service
	 * @param b
	 * @return
	 */
	public static EReference createEReference(EClass parent, String refName, EClass type, boolean containment) {
		EReference feature = EcoreFactory.eINSTANCE.createEReference();
		feature.setName(refName);
		feature.setEType(type);
		feature.setContainment(containment);
		parent.getEStructuralFeatures().add(feature);
		return feature;
	}

	/**
	 * TODO: What should we do if the type is unkwon?
	 * @param service
	 * @param resource
	 * @param type
	 * @return
	 */
	public static EAttribute createEAttribute(EClass service, String resource, Class<?> type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(resource);
		attribute.setEType(typeMap.get(type));
		service.getEStructuralFeatures().add(attribute);
		return attribute;
	}
	
}
