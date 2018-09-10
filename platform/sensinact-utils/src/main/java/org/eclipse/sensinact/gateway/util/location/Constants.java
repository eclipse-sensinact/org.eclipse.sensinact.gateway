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
package org.eclipse.sensinact.gateway.util.location;

public abstract class Constants {

	public static final double DOUBLE_PI = Math.PI * 2D;
	public static final double DEGREES_TO_RADIUS_COEF = Math.PI / 180D;
	public static final double RADIUS_TO_DEGREES_COEF = 180D / Math.PI;
	public static final double EARTH_SPHERICAL_MODEL_RADIUS = 6372.7976D;
	public static final double EARTH_ELIPSOID_MODEL_RADIUS = 6378.1370D;
	public static final double EARTH_POLAR_FLATTENING = 298.257223563D;


}
