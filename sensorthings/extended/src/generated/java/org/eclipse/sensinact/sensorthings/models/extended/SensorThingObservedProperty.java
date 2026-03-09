/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Kentyou - initial API and implementation 
 */
package org.eclipse.sensinact.sensorthings.models.extended;

import org.eclipse.sensinact.model.core.provider.DynamicProvider;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Sensor Thing Observed Property</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingObservedProperty#getObservedproperty <em>Observedproperty</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingObservedProperty()
 * @model
 * @generated
 */
@ProviderType
public interface SensorThingObservedProperty extends DynamicProvider {
	/**
	 * Returns the value of the '<em><b>Observedproperty</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observedproperty</em>' containment reference.
	 * @see #setObservedproperty(ObservedPropertyService)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getSensorThingObservedProperty_Observedproperty()
	 * @model containment="true"
	 * @generated
	 */
	ObservedPropertyService getObservedproperty();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.SensorThingObservedProperty#getObservedproperty <em>Observedproperty</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observedproperty</em>' containment reference.
	 * @see #getObservedproperty()
	 * @generated
	 */
	void setObservedproperty(ObservedPropertyService value);

} // SensorThingObservedProperty
