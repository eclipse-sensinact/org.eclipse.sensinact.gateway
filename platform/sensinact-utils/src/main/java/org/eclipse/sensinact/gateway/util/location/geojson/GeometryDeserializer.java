package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.List;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class GeometryDeserializer extends StdDeserializer<GeoJsonGeometry>{

	/**
	 * Long serial version ID
	 */
	private static final long serialVersionUID = 3068880107931191950L;

	protected GeometryDeserializer() {
		super(GeoJsonGeometry.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public GeoJsonGeometry deserialize(JsonParser p, DeserializationContext ctxt) 
			throws IOException, JsonProcessingException {
		
		TreeNode tn = p.readValueAsTree();
		GeoJsonGeometry geojsonGeometry = null;
		
		Geometry geometry;
		TreeNode coordinates;

		if (tn.get("type") != null) 
			geometry = Geometry.valueOf(treeNodeAsString(tn.get("type")));
		else
			return null;

		coordinates = tn.get("coordinates");
		
		if (coordinates == null || !coordinates.isArray()) 
			return null;
		
		switch(geometry) {
			case LineString:
				geojsonGeometry = new GeoJsonLineStringGeometry(
						(List<GeoJsonPosition>) p.getCodec().treeToValue(coordinates, List.class));
				break;
			case MultiLineString:
				geojsonGeometry = new GeoJsonMultiLineStringGeometry(
						(List<List<GeoJsonPosition>>) p.getCodec().treeToValue(coordinates, List.class));
				break;
			case MultiPoint:
				geojsonGeometry = new GeoJsonMultiPointGeometry(
						(List<GeoJsonPosition>) p.getCodec().treeToValue(coordinates, List.class));
				break;
			case MultiPolygon:
				geojsonGeometry = new GeoJsonMultiPolygonGeometry(
						(List<List<List<GeoJsonPosition>>>) p.getCodec().treeToValue(
								coordinates, List.class));
				break;
			case Point:
				double longitude = Double.parseDouble(treeNodeAsString(coordinates.get(0)));
				double latitude = Double.parseDouble(treeNodeAsString(coordinates.get(1)));
				geojsonGeometry = new GeoJsonPointGeometry(new GeoJsonPosition(longitude,latitude));
				break;
			case Polygon:
				geojsonGeometry = new GeoJsonPolygonGeometry(
						(List<List<GeoJsonPosition>>) p.getCodec().treeToValue(coordinates, List.class));
				break;
			default:
				break;
		}		 
		return geojsonGeometry;
	}
	
	
	String treeNodeAsString(TreeNode tn) {
		return tn.toString().replace('"', ' ').trim();
	}
	
}
