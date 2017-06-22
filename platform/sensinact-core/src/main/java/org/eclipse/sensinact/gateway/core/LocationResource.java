/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core;


/**
 * Extended {@link PropertyResource} defining a location
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface LocationResource extends PropertyResource
{
	/**
	 * Enumeration of types of LocationResources. The type of a LocationResource
	 * is useful for coordinates and distances interpretations and/or
	 * calculations
	 */
	public static enum LRType
	{
		/**
		 * Coordinates of this type of LocationResource are interpreted as
		 * being, in order, the latitude, the longitude and the altitude
		 */
		L_L_A_GEOCENTRIC,
		/**
		 * Coordinates of this type of LocationResource are interpreted as
		 * being, in order, the abscissa, the ordinate and the height in an
		 * orthonormal coordinate system
		 */
		X_Y_Z_ORTHONORMAL,
		/**
		 * Coordinates of this type of LocationResource are interpreted as
		 * being, in order, a linear distance from the origin of an orthonormal
		 * coordinate system, an angle defining the gradient, and the height.
		 */
		X_ALPHA_Z_ORTHONORMAL;
	}

	public static final String LOCATION = "location";

	public static final String UNKNOW_LOCATION = "unknown";

	/**
	 * the default linear measurement unit
	 */
	public static final String DEFAULT_LINEAR_MEASUREMENT_UNIT = "meter";

	/**
	 * the default angular measurement unit
	 */
	public static final String DEFAULT_ANGULAR_MEASUREMENT_UNIT = "degree";

	/* primitives of a location resource */
	public static final String ABSOLUTE_ATTRIBUTE_NAME = "absolute";
	public static final String LRTYPE_ATTRIBUTE_NAME = "lrtype";
	public static final String COORDINATES_ATTRIBUTE_NAME = "coordinates";
	public static final String LINEAR_MEASURE_UNIT_ATTRIBUTE_NAME = "linear-unit";
	public static final String ANGULAR_MEASURE_UNIT_ATTRIBUTE_NAME = "angular-unit";
	public static final String REFERENCE_POINT_ATTRIBUTE_NAME = "ref";

	public static final Class<?> ABSOLUTE_ATTRIBUTE_CLASS = Boolean.class;
	public static final Class<?> LRTYPE_ATTRIBUTE_CLASS = LRType.class;
	public static final Class<?> COORDINATES_ATTRIBUTE_CLASS = float[].class;
	public static final Class<?> LINEAR_MEASURE_UNIT_ATTRIBUTE_CLASS = String.class;
	public static final Class<?> ANGULAR_MEASURE_UNIT_ATTRIBUTE_CLASS = String.class;
	public static final Class<?> REFERENCE_POINT_ATTRIBUTE_CLASS = String.class;

	static final AttributeBuilder[] ATTRIBUTES = new AttributeBuilder[]
	{
        // absolute attribute cannot be modified
        new AttributeBuilder(ABSOLUTE_ATTRIBUTE_NAME, 
        		new AttributeBuilder.Requirement[]
        { AttributeBuilder.Requirement.TYPE }).type(
        		ABSOLUTE_ATTRIBUTE_CLASS).value(
                new Boolean(false)),

        new AttributeBuilder(LRTYPE_ATTRIBUTE_NAME, 
        		new AttributeBuilder.Requirement[]
        { AttributeBuilder.Requirement.TYPE }).type(
        		LRTYPE_ATTRIBUTE_CLASS).value(
                LRType.X_Y_Z_ORTHONORMAL),

        new AttributeBuilder(COORDINATES_ATTRIBUTE_NAME, 
        		new AttributeBuilder.Requirement[]
        { AttributeBuilder.Requirement.TYPE }).type(
        		COORDINATES_ATTRIBUTE_CLASS).value(
                new float[]{ 0.0f, 0.0f, 0.0f }),

        new AttributeBuilder(LINEAR_MEASURE_UNIT_ATTRIBUTE_NAME,
                new AttributeBuilder.Requirement[]
                { AttributeBuilder.Requirement.TYPE }).type(
                LINEAR_MEASURE_UNIT_ATTRIBUTE_CLASS).value(
                DEFAULT_LINEAR_MEASUREMENT_UNIT),

        new AttributeBuilder(ANGULAR_MEASURE_UNIT_ATTRIBUTE_NAME,
                new AttributeBuilder.Requirement[]
                { AttributeBuilder.Requirement.TYPE }).type(
                ANGULAR_MEASURE_UNIT_ATTRIBUTE_CLASS).value(
                DEFAULT_ANGULAR_MEASUREMENT_UNIT),

        new AttributeBuilder(REFERENCE_POINT_ATTRIBUTE_NAME,
                new AttributeBuilder.Requirement[]
                { AttributeBuilder.Requirement.TYPE }).type(
                		REFERENCE_POINT_ATTRIBUTE_CLASS) 
	};
}
