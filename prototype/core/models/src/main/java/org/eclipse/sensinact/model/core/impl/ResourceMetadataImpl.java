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
package org.eclipse.sensinact.model.core.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.model.core.ResourceMetadata;
import org.eclipse.sensinact.model.core.SensiNactPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Resource Metadata</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class ResourceMetadataImpl extends MetadataImpl implements ResourceMetadata {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ResourceMetadataImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return SensiNactPackage.Literals.RESOURCE_METADATA;
	}

} //ResourceMetadataImpl
