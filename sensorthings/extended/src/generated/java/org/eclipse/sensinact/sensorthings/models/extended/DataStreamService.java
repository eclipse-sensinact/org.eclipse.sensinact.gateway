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

import java.time.Instant;

import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

import org.eclipse.sensinact.model.core.provider.Service;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Stream Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorName <em>Sensor Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorDescription <em>Sensor Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorEncodingType <em>Sensor Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitName <em>Unit Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitSymbol <em>Unit Symbol</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitDefinition <em>Unit Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyName <em>Observed Property Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyId <em>Observed Property Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDescription <em>Observed Property Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDefinition <em>Observed Property Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorMetadata <em>Sensor Metadata</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorId <em>Sensor Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorProperties <em>Sensor Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyProperties <em>Observed Property Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationResultTime <em>Observation Result Time</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationResult <em>Observation Result</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationPhenomenonTime <em>Observation Phenomenon Time</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiName <em>Observation Foi Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiEncodingType <em>Observation Foi Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiId <em>Observation Foi Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationId <em>Observation Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationResultQuality <em>Observation Result Quality</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationValidTime <em>Observation Valid Time</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationParameters <em>Observation Parameters</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationProperties <em>Observation Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiFeature <em>Observation Foi Feature</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiDescription <em>Observation Foi Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getThingId <em>Thing Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService()
 * @model
 * @generated
 */
@ProviderType
public interface DataStreamService extends Service {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Timestamp</em>' attribute.
	 * @see #setTimestamp(Instant)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_Timestamp()
	 * @model dataType="org.eclipse.sensinact.model.core.provider.EInstant"
	 * @generated
	 */
	Instant getTimestamp();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getTimestamp <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Timestamp</em>' attribute.
	 * @see #getTimestamp()
	 * @generated
	 */
	void setTimestamp(Instant value);

	/**
	 * Returns the value of the '<em><b>Sensor Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Name</em>' attribute.
	 * @see #setSensorName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_SensorName()
	 * @model
	 * @generated
	 */
	String getSensorName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorName <em>Sensor Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Name</em>' attribute.
	 * @see #getSensorName()
	 * @generated
	 */
	void setSensorName(String value);

	/**
	 * Returns the value of the '<em><b>Sensor Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Description</em>' attribute.
	 * @see #setSensorDescription(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_SensorDescription()
	 * @model
	 * @generated
	 */
	String getSensorDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorDescription <em>Sensor Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Description</em>' attribute.
	 * @see #getSensorDescription()
	 * @generated
	 */
	void setSensorDescription(String value);

	/**
	 * Returns the value of the '<em><b>Sensor Encoding Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Encoding Type</em>' attribute.
	 * @see #setSensorEncodingType(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_SensorEncodingType()
	 * @model
	 * @generated
	 */
	String getSensorEncodingType();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorEncodingType <em>Sensor Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Encoding Type</em>' attribute.
	 * @see #getSensorEncodingType()
	 * @generated
	 */
	void setSensorEncodingType(String value);

	/**
	 * Returns the value of the '<em><b>Unit Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit Name</em>' attribute.
	 * @see #setUnitName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_UnitName()
	 * @model
	 * @generated
	 */
	String getUnitName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitName <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit Name</em>' attribute.
	 * @see #getUnitName()
	 * @generated
	 */
	void setUnitName(String value);

	/**
	 * Returns the value of the '<em><b>Unit Symbol</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit Symbol</em>' attribute.
	 * @see #setUnitSymbol(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_UnitSymbol()
	 * @model
	 * @generated
	 */
	String getUnitSymbol();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitSymbol <em>Unit Symbol</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit Symbol</em>' attribute.
	 * @see #getUnitSymbol()
	 * @generated
	 */
	void setUnitSymbol(String value);

	/**
	 * Returns the value of the '<em><b>Unit Definition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unit Definition</em>' attribute.
	 * @see #setUnitDefinition(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_UnitDefinition()
	 * @model
	 * @generated
	 */
	String getUnitDefinition();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getUnitDefinition <em>Unit Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unit Definition</em>' attribute.
	 * @see #getUnitDefinition()
	 * @generated
	 */
	void setUnitDefinition(String value);

	/**
	 * Returns the value of the '<em><b>Observed Property Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Name</em>' attribute.
	 * @see #setObservedPropertyName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservedPropertyName()
	 * @model
	 * @generated
	 */
	String getObservedPropertyName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyName <em>Observed Property Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Name</em>' attribute.
	 * @see #getObservedPropertyName()
	 * @generated
	 */
	void setObservedPropertyName(String value);

	/**
	 * Returns the value of the '<em><b>Observed Property Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Id</em>' attribute.
	 * @see #setObservedPropertyId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservedPropertyId()
	 * @model
	 * @generated
	 */
	String getObservedPropertyId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyId <em>Observed Property Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Id</em>' attribute.
	 * @see #getObservedPropertyId()
	 * @generated
	 */
	void setObservedPropertyId(String value);

	/**
	 * Returns the value of the '<em><b>Observed Property Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Description</em>' attribute.
	 * @see #setObservedPropertyDescription(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservedPropertyDescription()
	 * @model
	 * @generated
	 */
	String getObservedPropertyDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDescription <em>Observed Property Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Description</em>' attribute.
	 * @see #getObservedPropertyDescription()
	 * @generated
	 */
	void setObservedPropertyDescription(String value);

	/**
	 * Returns the value of the '<em><b>Observed Property Definition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Definition</em>' attribute.
	 * @see #setObservedPropertyDefinition(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservedPropertyDefinition()
	 * @model
	 * @generated
	 */
	String getObservedPropertyDefinition();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyDefinition <em>Observed Property Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Definition</em>' attribute.
	 * @see #getObservedPropertyDefinition()
	 * @generated
	 */
	void setObservedPropertyDefinition(String value);

	/**
	 * Returns the value of the '<em><b>Sensor Metadata</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Metadata</em>' attribute.
	 * @see #setSensorMetadata(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_SensorMetadata()
	 * @model
	 * @generated
	 */
	Object getSensorMetadata();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorMetadata <em>Sensor Metadata</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Metadata</em>' attribute.
	 * @see #getSensorMetadata()
	 * @generated
	 */
	void setSensorMetadata(Object value);

	/**
	 * Returns the value of the '<em><b>Sensor Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Id</em>' attribute.
	 * @see #setSensorId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_SensorId()
	 * @model
	 * @generated
	 */
	String getSensorId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorId <em>Sensor Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Id</em>' attribute.
	 * @see #getSensorId()
	 * @generated
	 */
	void setSensorId(String value);

	/**
	 * Returns the value of the '<em><b>Sensor Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sensor Properties</em>' attribute.
	 * @see #setSensorProperties(Map)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_SensorProperties()
	 * @model transient="true"
	 * @generated
	 */
	Map<?, ?> getSensorProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getSensorProperties <em>Sensor Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sensor Properties</em>' attribute.
	 * @see #getSensorProperties()
	 * @generated
	 */
	void setSensorProperties(Map<?, ?> value);

	/**
	 * Returns the value of the '<em><b>Observed Property Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observed Property Properties</em>' attribute.
	 * @see #setObservedPropertyProperties(Map)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservedPropertyProperties()
	 * @model transient="true"
	 * @generated
	 */
	Map<?, ?> getObservedPropertyProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservedPropertyProperties <em>Observed Property Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observed Property Properties</em>' attribute.
	 * @see #getObservedPropertyProperties()
	 * @generated
	 */
	void setObservedPropertyProperties(Map<?, ?> value);

	/**
	 * Returns the value of the '<em><b>Observation Result Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Result Time</em>' attribute.
	 * @see #setObservationResultTime(Instant)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationResultTime()
	 * @model dataType="org.eclipse.sensinact.model.core.provider.EInstant"
	 * @generated
	 */
	Instant getObservationResultTime();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationResultTime <em>Observation Result Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Result Time</em>' attribute.
	 * @see #getObservationResultTime()
	 * @generated
	 */
	void setObservationResultTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Observation Result</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Result</em>' attribute.
	 * @see #setObservationResult(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationResult()
	 * @model
	 * @generated
	 */
	Object getObservationResult();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationResult <em>Observation Result</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Result</em>' attribute.
	 * @see #getObservationResult()
	 * @generated
	 */
	void setObservationResult(Object value);

	/**
	 * Returns the value of the '<em><b>Observation Phenomenon Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Phenomenon Time</em>' attribute.
	 * @see #setObservationPhenomenonTime(Instant)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationPhenomenonTime()
	 * @model dataType="org.eclipse.sensinact.model.core.provider.EInstant"
	 * @generated
	 */
	Instant getObservationPhenomenonTime();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationPhenomenonTime <em>Observation Phenomenon Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Phenomenon Time</em>' attribute.
	 * @see #getObservationPhenomenonTime()
	 * @generated
	 */
	void setObservationPhenomenonTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Observation Foi Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Foi Name</em>' attribute.
	 * @see #setObservationFoiName(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationFoiName()
	 * @model
	 * @generated
	 */
	String getObservationFoiName();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiName <em>Observation Foi Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Foi Name</em>' attribute.
	 * @see #getObservationFoiName()
	 * @generated
	 */
	void setObservationFoiName(String value);

	/**
	 * Returns the value of the '<em><b>Observation Foi Encoding Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Foi Encoding Type</em>' attribute.
	 * @see #setObservationFoiEncodingType(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationFoiEncodingType()
	 * @model
	 * @generated
	 */
	String getObservationFoiEncodingType();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiEncodingType <em>Observation Foi Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Foi Encoding Type</em>' attribute.
	 * @see #getObservationFoiEncodingType()
	 * @generated
	 */
	void setObservationFoiEncodingType(String value);

	/**
	 * Returns the value of the '<em><b>Observation Foi Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Foi Id</em>' attribute.
	 * @see #setObservationFoiId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationFoiId()
	 * @model
	 * @generated
	 */
	String getObservationFoiId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiId <em>Observation Foi Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Foi Id</em>' attribute.
	 * @see #getObservationFoiId()
	 * @generated
	 */
	void setObservationFoiId(String value);

	/**
	 * Returns the value of the '<em><b>Observation Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Id</em>' attribute.
	 * @see #setObservationId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationId()
	 * @model
	 * @generated
	 */
	String getObservationId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationId <em>Observation Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Id</em>' attribute.
	 * @see #getObservationId()
	 * @generated
	 */
	void setObservationId(String value);

	/**
	 * Returns the value of the '<em><b>Observation Result Quality</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Result Quality</em>' attribute.
	 * @see #setObservationResultQuality(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationResultQuality()
	 * @model
	 * @generated
	 */
	String getObservationResultQuality();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationResultQuality <em>Observation Result Quality</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Result Quality</em>' attribute.
	 * @see #getObservationResultQuality()
	 * @generated
	 */
	void setObservationResultQuality(String value);

	/**
	 * Returns the value of the '<em><b>Observation Valid Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Valid Time</em>' attribute.
	 * @see #setObservationValidTime(Object)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationValidTime()
	 * @model
	 * @generated
	 */
	Object getObservationValidTime();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationValidTime <em>Observation Valid Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Valid Time</em>' attribute.
	 * @see #getObservationValidTime()
	 * @generated
	 */
	void setObservationValidTime(Object value);

	/**
	 * Returns the value of the '<em><b>Observation Parameters</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Parameters</em>' attribute.
	 * @see #setObservationParameters(Map)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationParameters()
	 * @model transient="true"
	 * @generated
	 */
	Map<?, ?> getObservationParameters();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationParameters <em>Observation Parameters</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Parameters</em>' attribute.
	 * @see #getObservationParameters()
	 * @generated
	 */
	void setObservationParameters(Map<?, ?> value);

	/**
	 * Returns the value of the '<em><b>Observation Properties</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Properties</em>' attribute.
	 * @see #setObservationProperties(Map)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationProperties()
	 * @model transient="true"
	 * @generated
	 */
	Map<?, ?> getObservationProperties();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationProperties <em>Observation Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Properties</em>' attribute.
	 * @see #getObservationProperties()
	 * @generated
	 */
	void setObservationProperties(Map<?, ?> value);

	/**
	 * Returns the value of the '<em><b>Observation Foi Feature</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Foi Feature</em>' attribute.
	 * @see #setObservationFoiFeature(GeoJsonObject)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationFoiFeature()
	 * @model dataType="org.eclipse.sensinact.model.core.provider.EGeoJsonObject"
	 * @generated
	 */
	GeoJsonObject getObservationFoiFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiFeature <em>Observation Foi Feature</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Foi Feature</em>' attribute.
	 * @see #getObservationFoiFeature()
	 * @generated
	 */
	void setObservationFoiFeature(GeoJsonObject value);

	/**
	 * Returns the value of the '<em><b>Observation Foi Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Observation Foi Description</em>' attribute.
	 * @see #setObservationFoiDescription(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ObservationFoiDescription()
	 * @model
	 * @generated
	 */
	String getObservationFoiDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getObservationFoiDescription <em>Observation Foi Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Observation Foi Description</em>' attribute.
	 * @see #getObservationFoiDescription()
	 * @generated
	 */
	void setObservationFoiDescription(String value);

	/**
	 * Returns the value of the '<em><b>Thing Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Thing Id</em>' attribute.
	 * @see #setThingId(String)
	 * @see org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage#getDataStreamService_ThingId()
	 * @model
	 * @generated
	 */
	String getThingId();

	/**
	 * Sets the value of the '{@link org.eclipse.sensinact.sensorthings.models.extended.DataStreamService#getThingId <em>Thing Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Thing Id</em>' attribute.
	 * @see #getThingId()
	 * @generated
	 */
	void setThingId(String value);

} // DataStreamService
