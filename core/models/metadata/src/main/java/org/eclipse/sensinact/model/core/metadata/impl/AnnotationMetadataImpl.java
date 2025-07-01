/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion - initial API and implementation 
 */
package org.eclipse.sensinact.model.core.metadata.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.sensinact.model.core.metadata.AnnotationMetadata;
import org.eclipse.sensinact.model.core.metadata.MetadataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Annotation Metadata</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class AnnotationMetadataImpl extends NexusMetadataImpl implements AnnotationMetadata {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AnnotationMetadataImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MetadataPackage.Literals.ANNOTATION_METADATA;
	}

} //AnnotationMetadataImpl
