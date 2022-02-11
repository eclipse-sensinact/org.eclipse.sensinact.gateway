package org.eclipse.sensinact.gateway.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.gateway.util.location.Point;
import org.eclipse.sensinact.gateway.util.location.geojson.GeoJson;
import org.eclipse.sensinact.gateway.util.location.geojson.GeoJsonFeature;
import org.eclipse.sensinact.gateway.util.location.geojson.GeoJsonGeometry;
import org.eclipse.sensinact.gateway.util.location.geojson.GeoJsonPointGeometry;
import org.eclipse.sensinact.gateway.util.location.geojson.GeoJsonPosition;
import org.eclipse.sensinact.gateway.util.location.geojson.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GeoJsonUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(GeoJsonUtils.class); 
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static GeoJson readGeoJson(String s) {
		try {
			String gs = s.replace("\\\"","\"");
			GeoJson g = MAPPER.readValue(gs, GeoJson.class);			
			return g;			
		} catch(Exception e) {
			LOG.error(e.getMessage(),e);
		}
		return null;
	}
	
	public static List<Point> getPointsFromGeoJson(String s) {
		GeoJson g =readGeoJson(s);			
		if(g == null)
			return Collections.emptyList();
		return getPointsFromGeoJson(g);
	}
	
	public static List<Point> getPointsFromGeoJson(GeoJson g) {
		List<Point> points = g.getFeatures().stream().filter(x -> Geometry.Point.equals(
			x.getGeometry().getType())).map( x ->new Point(
				((GeoJsonPointGeometry) x.getGeometry()).getCoordinates().getLatitude(),
				((GeoJsonPointGeometry) x.getGeometry()).getCoordinates().getLongitude())
				).collect(Collectors.toList());			
		return points;
	}

	public static Point getFirstPointFromGeoJsonPoint(String s) {
			List<Point> points = getPointsFromGeoJson(s);
			if(points.isEmpty())
				return null;
			return points.get(0);
	}

	public static Point getFirstPointFromGeoJsonPoint(GeoJson g) {
			List<Point> points = getPointsFromGeoJson(g);
			if(points.isEmpty())
				return null;
			return points.get(0);
	}

	public static GeoJson getGeoJsonPointFromPoints(List<Point> points) {
		if(points == null || points.isEmpty())
			return new GeoJson("FeatureCollection",Collections.emptyList()); 
		return new GeoJson("FeatureCollection", points.stream().map( p -> {
			GeoJsonPosition position = new GeoJsonPosition(p.longitude, p.latitude);
			GeoJsonGeometry geometry = new GeoJsonPointGeometry(position);
			GeoJsonFeature feature = new GeoJsonFeature("Feature", Collections.emptyMap(), geometry);
			return feature;
		}).collect(Collectors.toList()));
	}

	public static GeoJson getGeoJsonPointFromPoint(Point point) {
		if(point == null)
			return getGeoJsonPointFromPoints(Collections.emptyList());
		return getGeoJsonPointFromPoints(Collections.singletonList(point));
	}
	
	public static GeoJson getGeoJsonPointFromCoordinates(double[] coordinates) {
		if(coordinates!=null && coordinates.length == 2)
			return getGeoJsonPointFromPoint(new Point(coordinates[0],coordinates[1]));		
		return new GeoJson("FeatureCollection",Collections.emptyList()); 	
	}

	public GeoJson getGeoJsonPointFromStringCoordinates(String s) {
		String[] coordinates = (s==null||s.length()==0)?null:s.split(":");
		if(coordinates!=null && coordinates.length == 2){
			try {
				return getGeoJsonPointFromPoint(new Point(
					Double.parseDouble(coordinates[0]),
					Double.parseDouble(coordinates[1])));
			} catch(IllegalArgumentException e) {
				LOG.error(e.getMessage(),e);
			}
		}
		return new GeoJson("FeatureCollection",Collections.emptyList()); 		
	}
	
	public static String geoJsonToString(GeoJson geojson) {
		try {
			return MAPPER.writeValueAsString(geojson);
		} catch (JsonProcessingException e) {
			LOG.error(e.getMessage(),e);
		}
		return null;
	}

	public static String geoJsonFeaturesToString(GeoJson geojson) {
		return  geojson.getFeatures().stream().map(
			f -> {
				try {
					return MAPPER.writeValueAsString(f);
				} catch (JsonProcessingException e) {
					LOG.error(e.getMessage(),e);
					return "{}";
				}
			}).collect(Collectors.joining(","));
	}
}
