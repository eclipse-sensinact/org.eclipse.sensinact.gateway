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
package org.eclipse.sensinact.gateway.nthbnd.filter.geojson.test;


import static org.junit.Assert.assertTrue;

import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.internal.GeoJSONFiltering;
import org.eclipse.sensinact.gateway.nthbnd.filter.geojson.internal.GeoJSONFiltering.Segment;
import org.junit.Test;


/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class CalculationsTest{

	@Test
	public void testDistanceCalulation()
	{
		Segment segment = GeoJSONFiltering.getElipsoidEarthModelDistance(45.1855758793d, 5.7326316833d, 45.1887820012d, 5.740571022d);		
		double distance =  GeoJSONFiltering.getDistance(45.1855758793d, 5.7326316833d, 45.1887820012d, 5.740571022d);
		int distanceInt = (int) Math.round(distance);
		assertTrue(Math.abs(719-distanceInt)<5d);
		System.out.println(distance);
		
		distance =  segment.getDistance();
		distanceInt = (int) Math.round(distance);
		assertTrue(Math.abs(719-distanceInt)<5d);		
		System.out.println(distance);
		System.out.println(segment.getForwardAzimuth());
		System.out.println(segment.getBackAzimuth());		
	}
}
