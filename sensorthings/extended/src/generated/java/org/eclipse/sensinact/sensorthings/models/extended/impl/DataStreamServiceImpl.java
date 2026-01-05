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
package org.eclipse.sensinact.sensorthings.models.extended.impl;

import java.time.Instant;

import java.util.Map;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.sensinact.model.core.provider.impl.ServiceImpl;

import org.eclipse.sensinact.sensorthings.models.extended.DataStreamService;
import org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Stream Service</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getTimestamp <em>Timestamp</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorName <em>Sensor Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorDescription <em>Sensor Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorEncodingType <em>Sensor Encoding Type</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getUnitName <em>Unit Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getUnitSymbol <em>Unit Symbol</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getUnitDefinition <em>Unit Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservedPropertyName <em>Observed Property Name</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservedPropertyId <em>Observed Property Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservedPropertyDescription <em>Observed Property Description</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservedPropertyDefinition <em>Observed Property Definition</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorMetadata <em>Sensor Metadata</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorId <em>Sensor Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getSensorProperties <em>Sensor Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getObservedPropertyProperties <em>Observed Property Properties</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getThingId <em>Thing Id</em>}</li>
 *   <li>{@link org.eclipse.sensinact.sensorthings.models.extended.impl.DataStreamServiceImpl#getLastObservation <em>Last Observation</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataStreamServiceImpl extends ServiceImpl implements DataStreamService {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getTimestamp() <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimestamp()
	 * @generated
	 * @ordered
	 */
	protected static final Instant TIMESTAMP_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTimestamp() <em>Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTimestamp()
	 * @generated
	 * @ordered
	 */
	protected Instant timestamp = TIMESTAMP_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorName() <em>Sensor Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorName()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorName() <em>Sensor Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorName()
	 * @generated
	 * @ordered
	 */
	protected String sensorName = SENSOR_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorDescription() <em>Sensor Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorDescription() <em>Sensor Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorDescription()
	 * @generated
	 * @ordered
	 */
	protected String sensorDescription = SENSOR_DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorEncodingType() <em>Sensor Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorEncodingType()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_ENCODING_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorEncodingType() <em>Sensor Encoding Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorEncodingType()
	 * @generated
	 * @ordered
	 */
	protected String sensorEncodingType = SENSOR_ENCODING_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitName() <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitName()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitName() <em>Unit Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitName()
	 * @generated
	 * @ordered
	 */
	protected String unitName = UNIT_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitSymbol() <em>Unit Symbol</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitSymbol()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_SYMBOL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitSymbol() <em>Unit Symbol</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitSymbol()
	 * @generated
	 * @ordered
	 */
	protected String unitSymbol = UNIT_SYMBOL_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnitDefinition() <em>Unit Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitDefinition()
	 * @generated
	 * @ordered
	 */
	protected static final String UNIT_DEFINITION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnitDefinition() <em>Unit Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnitDefinition()
	 * @generated
	 * @ordered
	 */
	protected String unitDefinition = UNIT_DEFINITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservedPropertyName() <em>Observed Property Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyName()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedPropertyName() <em>Observed Property Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyName()
	 * @generated
	 * @ordered
	 */
	protected String observedPropertyName = OBSERVED_PROPERTY_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservedPropertyId() <em>Observed Property Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyId()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedPropertyId() <em>Observed Property Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyId()
	 * @generated
	 * @ordered
	 */
	protected String observedPropertyId = OBSERVED_PROPERTY_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservedPropertyDescription() <em>Observed Property Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedPropertyDescription() <em>Observed Property Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyDescription()
	 * @generated
	 * @ordered
	 */
	protected String observedPropertyDescription = OBSERVED_PROPERTY_DESCRIPTION_EDEFAULT;

	/**
	 * The default value of the '{@link #getObservedPropertyDefinition() <em>Observed Property Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyDefinition()
	 * @generated
	 * @ordered
	 */
	protected static final String OBSERVED_PROPERTY_DEFINITION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getObservedPropertyDefinition() <em>Observed Property Definition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyDefinition()
	 * @generated
	 * @ordered
	 */
	protected String observedPropertyDefinition = OBSERVED_PROPERTY_DEFINITION_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorMetadata() <em>Sensor Metadata</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorMetadata()
	 * @generated
	 * @ordered
	 */
	protected static final Object SENSOR_METADATA_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorMetadata() <em>Sensor Metadata</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorMetadata()
	 * @generated
	 * @ordered
	 */
	protected Object sensorMetadata = SENSOR_METADATA_EDEFAULT;

	/**
	 * The default value of the '{@link #getSensorId() <em>Sensor Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorId()
	 * @generated
	 * @ordered
	 */
	protected static final String SENSOR_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSensorId() <em>Sensor Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorId()
	 * @generated
	 * @ordered
	 */
	protected String sensorId = SENSOR_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSensorProperties() <em>Sensor Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSensorProperties()
	 * @generated
	 * @ordered
	 */
	protected Map<?, ?> sensorProperties;

	/**
	 * The cached value of the '{@link #getObservedPropertyProperties() <em>Observed Property Properties</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getObservedPropertyProperties()
	 * @generated
	 * @ordered
	 */
	protected Map<?, ?> observedPropertyProperties;

	/**
	 * The default value of the '{@link #getThingId() <em>Thing Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThingId()
	 * @generated
	 * @ordered
	 */
	protected static final String THING_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getThingId() <em>Thing Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThingId()
	 * @generated
	 * @ordered
	 */
	protected String thingId = THING_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastObservation() <em>Last Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastObservation()
	 * @generated
	 * @ordered
	 */
	protected static final Object LAST_OBSERVATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastObservation() <em>Last Observation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastObservation()
	 * @generated
	 * @ordered
	 */
	protected Object lastObservation = LAST_OBSERVATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataStreamServiceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ExtendedPackage.Literals.DATA_STREAM_SERVICE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTimestamp(Instant newTimestamp) {
		Instant oldTimestamp = timestamp;
		timestamp = newTimestamp;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP, oldTimestamp, timestamp));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensorName() {
		return sensorName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorName(String newSensorName) {
		String oldSensorName = sensorName;
		sensorName = newSensorName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_NAME, oldSensorName, sensorName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensorDescription() {
		return sensorDescription;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorDescription(String newSensorDescription) {
		String oldSensorDescription = sensorDescription;
		sensorDescription = newSensorDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_DESCRIPTION, oldSensorDescription, sensorDescription));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensorEncodingType() {
		return sensorEncodingType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorEncodingType(String newSensorEncodingType) {
		String oldSensorEncodingType = sensorEncodingType;
		sensorEncodingType = newSensorEncodingType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE, oldSensorEncodingType, sensorEncodingType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitName() {
		return unitName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitName(String newUnitName) {
		String oldUnitName = unitName;
		unitName = newUnitName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME, oldUnitName, unitName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitSymbol() {
		return unitSymbol;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitSymbol(String newUnitSymbol) {
		String oldUnitSymbol = unitSymbol;
		unitSymbol = newUnitSymbol;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL, oldUnitSymbol, unitSymbol));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUnitDefinition() {
		return unitDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnitDefinition(String newUnitDefinition) {
		String oldUnitDefinition = unitDefinition;
		unitDefinition = newUnitDefinition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION, oldUnitDefinition, unitDefinition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedPropertyName() {
		return observedPropertyName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyName(String newObservedPropertyName) {
		String oldObservedPropertyName = observedPropertyName;
		observedPropertyName = newObservedPropertyName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME, oldObservedPropertyName, observedPropertyName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedPropertyId() {
		return observedPropertyId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyId(String newObservedPropertyId) {
		String oldObservedPropertyId = observedPropertyId;
		observedPropertyId = newObservedPropertyId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID, oldObservedPropertyId, observedPropertyId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedPropertyDescription() {
		return observedPropertyDescription;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyDescription(String newObservedPropertyDescription) {
		String oldObservedPropertyDescription = observedPropertyDescription;
		observedPropertyDescription = newObservedPropertyDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION, oldObservedPropertyDescription, observedPropertyDescription));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getObservedPropertyDefinition() {
		return observedPropertyDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyDefinition(String newObservedPropertyDefinition) {
		String oldObservedPropertyDefinition = observedPropertyDefinition;
		observedPropertyDefinition = newObservedPropertyDefinition;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION, oldObservedPropertyDefinition, observedPropertyDefinition));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getSensorMetadata() {
		return sensorMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorMetadata(Object newSensorMetadata) {
		Object oldSensorMetadata = sensorMetadata;
		sensorMetadata = newSensorMetadata;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_METADATA, oldSensorMetadata, sensorMetadata));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSensorId() {
		return sensorId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorId(String newSensorId) {
		String oldSensorId = sensorId;
		sensorId = newSensorId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID, oldSensorId, sensorId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<?, ?> getSensorProperties() {
		return sensorProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSensorProperties(Map<?, ?> newSensorProperties) {
		Map<?, ?> oldSensorProperties = sensorProperties;
		sensorProperties = newSensorProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_PROPERTIES, oldSensorProperties, sensorProperties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Map<?, ?> getObservedPropertyProperties() {
		return observedPropertyProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setObservedPropertyProperties(Map<?, ?> newObservedPropertyProperties) {
		Map<?, ?> oldObservedPropertyProperties = observedPropertyProperties;
		observedPropertyProperties = newObservedPropertyProperties;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES, oldObservedPropertyProperties, observedPropertyProperties));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getThingId() {
		return thingId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThingId(String newThingId) {
		String oldThingId = thingId;
		thingId = newThingId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__THING_ID, oldThingId, thingId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object getLastObservation() {
		return lastObservation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastObservation(Object newLastObservation) {
		Object oldLastObservation = lastObservation;
		lastObservation = newLastObservation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION, oldLastObservation, lastObservation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				return getId();
			case ExtendedPackage.DATA_STREAM_SERVICE__NAME:
				return getName();
			case ExtendedPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				return getDescription();
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				return getTimestamp();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_NAME:
				return getSensorName();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_DESCRIPTION:
				return getSensorDescription();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE:
				return getSensorEncodingType();
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				return getUnitName();
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				return getUnitSymbol();
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				return getUnitDefinition();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME:
				return getObservedPropertyName();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				return getObservedPropertyId();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION:
				return getObservedPropertyDescription();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				return getObservedPropertyDefinition();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_METADATA:
				return getSensorMetadata();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				return getSensorId();
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_PROPERTIES:
				return getSensorProperties();
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				return getObservedPropertyProperties();
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				return getThingId();
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				return getLastObservation();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				setId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__NAME:
				setName((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				setTimestamp((Instant)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_NAME:
				setSensorName((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_DESCRIPTION:
				setSensorDescription((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE:
				setSensorEncodingType((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				setUnitName((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				setUnitSymbol((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				setUnitDefinition((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME:
				setObservedPropertyName((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				setObservedPropertyId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION:
				setObservedPropertyDescription((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				setObservedPropertyDefinition((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_METADATA:
				setSensorMetadata(newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				setSensorId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_PROPERTIES:
				setSensorProperties((Map<?, ?>)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				setObservedPropertyProperties((Map<?, ?>)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				setThingId((String)newValue);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				setLastObservation(newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				setId(ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				setTimestamp(TIMESTAMP_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_NAME:
				setSensorName(SENSOR_NAME_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_DESCRIPTION:
				setSensorDescription(SENSOR_DESCRIPTION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE:
				setSensorEncodingType(SENSOR_ENCODING_TYPE_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				setUnitName(UNIT_NAME_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				setUnitSymbol(UNIT_SYMBOL_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				setUnitDefinition(UNIT_DEFINITION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME:
				setObservedPropertyName(OBSERVED_PROPERTY_NAME_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				setObservedPropertyId(OBSERVED_PROPERTY_ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION:
				setObservedPropertyDescription(OBSERVED_PROPERTY_DESCRIPTION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				setObservedPropertyDefinition(OBSERVED_PROPERTY_DEFINITION_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_METADATA:
				setSensorMetadata(SENSOR_METADATA_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				setSensorId(SENSOR_ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_PROPERTIES:
				setSensorProperties((Map<?, ?>)null);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				setObservedPropertyProperties((Map<?, ?>)null);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				setThingId(THING_ID_EDEFAULT);
				return;
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				setLastObservation(LAST_OBSERVATION_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ExtendedPackage.DATA_STREAM_SERVICE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ExtendedPackage.DATA_STREAM_SERVICE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ExtendedPackage.DATA_STREAM_SERVICE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case ExtendedPackage.DATA_STREAM_SERVICE__TIMESTAMP:
				return TIMESTAMP_EDEFAULT == null ? timestamp != null : !TIMESTAMP_EDEFAULT.equals(timestamp);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_NAME:
				return SENSOR_NAME_EDEFAULT == null ? sensorName != null : !SENSOR_NAME_EDEFAULT.equals(sensorName);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_DESCRIPTION:
				return SENSOR_DESCRIPTION_EDEFAULT == null ? sensorDescription != null : !SENSOR_DESCRIPTION_EDEFAULT.equals(sensorDescription);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ENCODING_TYPE:
				return SENSOR_ENCODING_TYPE_EDEFAULT == null ? sensorEncodingType != null : !SENSOR_ENCODING_TYPE_EDEFAULT.equals(sensorEncodingType);
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_NAME:
				return UNIT_NAME_EDEFAULT == null ? unitName != null : !UNIT_NAME_EDEFAULT.equals(unitName);
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_SYMBOL:
				return UNIT_SYMBOL_EDEFAULT == null ? unitSymbol != null : !UNIT_SYMBOL_EDEFAULT.equals(unitSymbol);
			case ExtendedPackage.DATA_STREAM_SERVICE__UNIT_DEFINITION:
				return UNIT_DEFINITION_EDEFAULT == null ? unitDefinition != null : !UNIT_DEFINITION_EDEFAULT.equals(unitDefinition);
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_NAME:
				return OBSERVED_PROPERTY_NAME_EDEFAULT == null ? observedPropertyName != null : !OBSERVED_PROPERTY_NAME_EDEFAULT.equals(observedPropertyName);
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_ID:
				return OBSERVED_PROPERTY_ID_EDEFAULT == null ? observedPropertyId != null : !OBSERVED_PROPERTY_ID_EDEFAULT.equals(observedPropertyId);
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DESCRIPTION:
				return OBSERVED_PROPERTY_DESCRIPTION_EDEFAULT == null ? observedPropertyDescription != null : !OBSERVED_PROPERTY_DESCRIPTION_EDEFAULT.equals(observedPropertyDescription);
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_DEFINITION:
				return OBSERVED_PROPERTY_DEFINITION_EDEFAULT == null ? observedPropertyDefinition != null : !OBSERVED_PROPERTY_DEFINITION_EDEFAULT.equals(observedPropertyDefinition);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_METADATA:
				return SENSOR_METADATA_EDEFAULT == null ? sensorMetadata != null : !SENSOR_METADATA_EDEFAULT.equals(sensorMetadata);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_ID:
				return SENSOR_ID_EDEFAULT == null ? sensorId != null : !SENSOR_ID_EDEFAULT.equals(sensorId);
			case ExtendedPackage.DATA_STREAM_SERVICE__SENSOR_PROPERTIES:
				return sensorProperties != null;
			case ExtendedPackage.DATA_STREAM_SERVICE__OBSERVED_PROPERTY_PROPERTIES:
				return observedPropertyProperties != null;
			case ExtendedPackage.DATA_STREAM_SERVICE__THING_ID:
				return THING_ID_EDEFAULT == null ? thingId != null : !THING_ID_EDEFAULT.equals(thingId);
			case ExtendedPackage.DATA_STREAM_SERVICE__LAST_OBSERVATION:
				return LAST_OBSERVATION_EDEFAULT == null ? lastObservation != null : !LAST_OBSERVATION_EDEFAULT.equals(lastObservation);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (id: ");
		result.append(id);
		result.append(", name: ");
		result.append(name);
		result.append(", description: ");
		result.append(description);
		result.append(", timestamp: ");
		result.append(timestamp);
		result.append(", sensorName: ");
		result.append(sensorName);
		result.append(", sensorDescription: ");
		result.append(sensorDescription);
		result.append(", sensorEncodingType: ");
		result.append(sensorEncodingType);
		result.append(", unitName: ");
		result.append(unitName);
		result.append(", unitSymbol: ");
		result.append(unitSymbol);
		result.append(", unitDefinition: ");
		result.append(unitDefinition);
		result.append(", observedPropertyName: ");
		result.append(observedPropertyName);
		result.append(", observedPropertyId: ");
		result.append(observedPropertyId);
		result.append(", observedPropertyDescription: ");
		result.append(observedPropertyDescription);
		result.append(", observedPropertyDefinition: ");
		result.append(observedPropertyDefinition);
		result.append(", sensorMetadata: ");
		result.append(sensorMetadata);
		result.append(", sensorId: ");
		result.append(sensorId);
		result.append(", sensorProperties: ");
		result.append(sensorProperties);
		result.append(", observedPropertyProperties: ");
		result.append(observedPropertyProperties);
		result.append(", thingId: ");
		result.append(thingId);
		result.append(", lastObservation: ");
		result.append(lastObservation);
		result.append(')');
		return result.toString();
	}

} //DataStreamServiceImpl
