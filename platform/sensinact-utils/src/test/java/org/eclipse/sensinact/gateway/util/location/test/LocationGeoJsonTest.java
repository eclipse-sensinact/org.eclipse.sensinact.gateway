/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.util.location.test;


import org.eclipse.sensinact.gateway.util.location.geojson.GeoJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class LocationGeoJsonTest{

	String geo = "{"+
			"       \"type\": \"FeatureCollection\","+
			"       \"features\": [{"+
			"           \"type\": \"Feature\","+
			"           \"geometry\": {"+
			"               \"type\": \"Point\","+
			"               \"coordinates\": [102.0, 0.5]"+
			"           },"+
			"           \"properties\": {"+
			"               \"prop0\": \"value0\""+
			"           }"+
			"       }, {"+
			"           \"type\": \"Feature\","+
			"           \"geometry\": {"+
			"               \"type\": \"LineString\","+
			"               \"coordinates\": ["+
			"                   [102.0, 0.0],"+
			"                   [103.0, 1.0],"+
			"                   [104.0, 0.0],"+
			"                   [105.0, 1.0]"+
			"               ]"+
			"           },"+
			"           \"properties\": {"+
			"               \"prop0\": \"value0\","+
			"               \"prop1\": 0.0"+
			"           }"+
			"       }, {"+
			"           \"type\": \"Feature\","+
			"           \"geometry\": {"+
			"               \"type\": \"Polygon\","+
			"               \"coordinates\": ["+
			"                   ["+
			"                       [100.0, 0.0],"+
			"                       [101.0, 0.0],"+
			"                       [101.0, 1.0],"+
			"                       [100.0, 1.0],"+
			"                       [100.0, 0.0]"+
			"                   ]"+
			"               ]"+
			"           },"+
			"           \"properties\": {"+
			"               \"prop0\": \"value0\","+
			"               \"prop1\": {"+
			"                   \"this\": \"that\""+
			"               }"+
			"           }"+
			"       }]"+
			"   }";
	
	@Test
	public void testDeserialize() throws JsonMappingException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		GeoJson geojson= mapper.readValue(geo,GeoJson.class);
		geojson.getFeatures().stream().forEach(
				f -> {
					try {
						System.out.println(mapper.writer().writeValueAsString(f));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					switch(f.getGeometryType()) {
					case LineString:
						Assertions.assertEquals("[[102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]]",f.getGeometry().toString());
						break;
					case Point:
						Assertions.assertEquals("[102.0, 0.5]",f.getGeometry().toString());
						break;
					case Polygon:
						Assertions.assertEquals("[[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]",f.getGeometry().toString());
						break;
					default:
						break;					
					}
				});
	}

}
