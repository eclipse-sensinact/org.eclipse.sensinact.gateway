/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

/**
 * 
 * @author grune
 * @since Apr 18, 2024
 */
public class SensorthingsAnnotations {

    /**
     * Marks a resource as observed area
     */
    public static final String SENSORTHINGS_OBSERVEDAREA = "sensorthings.observedArea";
    /**
     *  Marks resource as observed property definition 
     */
    public static final String SENSORTHINGS_OBSERVEDPROPERTY_DEFINITION = "sensorthings.observedproperty.definition";
    /**
     *  Marks resource as observation quality 
     */
    public static final String SENSORTHINGS_OBSERVATION_QUALITY = "sensorthings.observation.quality";
    /**
     *  Marks resource with sensor encoding type 
     */
    public static final String SENSORTHINGS_SENSOR_ENCODING_TYPE = "sensorthings.sensor.encodingType";
    /**
     *  Marks resource with sensor metadata 
     */
    public static final String SENSORTHINGS_SENSOR_METADATA = "sensorthings.sensor.metadata";
    /**
     *  Marks resource with unit definition 
     */
    public static final String SENSORTHINGS_UNIT_DEFINITION = "sensorthings.unit.definition";
    /**
     *  Marks resource containing the name of the unit
     */
    public static final String SENSORTHINGS_UNIT_NAME = "sensorthings.unit.name";

    private SensorthingsAnnotations() {
    }
}
