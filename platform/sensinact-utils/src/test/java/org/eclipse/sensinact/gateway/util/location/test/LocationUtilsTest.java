/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.location.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.sensinact.gateway.util.LocationUtils;
import org.eclipse.sensinact.gateway.util.location.Point;
import org.eclipse.sensinact.gateway.util.location.Segment;
import org.junit.jupiter.api.Test;



/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class LocationUtilsTest{

	@Test
	public void testDistanceCalulation()
	{
		Segment segment = LocationUtils.getElipsoidEarthModelDistance(45.1855758793d, 5.7326316833d, 45.1887820012d, 5.740571022d);		
		double distance =  LocationUtils.getDistance(45.1855758793d, 5.7326316833d, 45.1887820012d, 5.740571022d);
		int distanceInt = (int) Math.round(distance);
		assertTrue(Math.abs(719-distanceInt)<5d);
		
		distance =  segment.getDistance();
		distanceInt = (int) Math.round(distance);
		assertTrue(Math.abs(719-distanceInt)<5d);
	}

	@Test
	public void testIntersectionFinding()
	{		      
		Segment segment1 = LocationUtils.getElipsoidEarthModelDistance(45.187972926197865,5.733747482299805,45.189719606541445,5.735764503479004);
		Segment segment2 = LocationUtils.getElipsoidEarthModelDistance(45.18810147160006,5.7350993156433105,45.19002205656789,5.7336509227752686);
		Segment intersection = LocationUtils.getSegmentIntersection(segment1, segment2);
		
		double latitude = intersection.getLat2();
		double longitude = intersection.getLng2();
		//effective intersection
		double lat = 45.188736632855935d; 
		double lng = 5.734624564647675d;
		
		double d = LocationUtils.getDistance(latitude, longitude, lat, lng);
		System.out.println(latitude+","+longitude);
		System.out.println(d);
		System.out.println(intersection.toString());
		assertTrue(Math.abs(d)<5d);
	}

	@Test
	public void testEFAPDecoding()
	{
    	 assertEquals(-179.98321d,LocationUtils.fromEPAF("`~oia@").get(0).doubleValue(),1e-5); 	 
    	 assertEquals("[38.5, -120.2, 40.7, -120.95, 43.252, -126.453]",LocationUtils.fromEPAF("_p~iF~ps|U_ulLnnqC_mqNvxq`@").toString());
    	 assertEquals(68,LocationUtils.epafToPoints("skyrGe~_b@K_@KYI_@CYA]A[@S@UJBFb@LRJDNDJ?b@KNKb@Wj@]vA{@NMLKvAsA^]BCnBaCLOFN\\[z@]r@Kv@DbAFDB^TJE`Bk@nAc@HEHNDFJRbAfB~@dBv@rAHLV`@JZHQDGV@HCHKfAQB?HALA?P\\rAFV?VRt@Tx@Lb@V^U`@IPj@nB").size());
	}

	@Test
	public void testEFAPEncoding()
	{
    	assertEquals("`~oia@",LocationUtils.toEPAF(-179.9832104d));    	 
    	assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@",LocationUtils.toEPAF(new Point[] {
    			 new Point(38.5d,-120.2d),
    			 new Point(40.7d,-120.95d),
    			 new Point(43.252d,-126.453d)
    	 }));
	}
}
