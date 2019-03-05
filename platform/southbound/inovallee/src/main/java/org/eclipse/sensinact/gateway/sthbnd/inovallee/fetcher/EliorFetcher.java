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
package org.eclipse.sensinact.gateway.sthbnd.inovallee.fetcher;

import java.io.IOException;

import org.eclipse.sensinact.gateway.generic.model.Tree;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.http.BasicHttpClient;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.http.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class EliorFetcher extends Fetcher {

    private final BasicHttpClient client = new BasicHttpClient(); 
	private final String url = "http://193.48.18.251:8095/restaurants/aggregated";
	//private final String url = "http://localhost:8095/restaurants/aggregated";
	
	
	public Tree fetch() throws IOException {

		Response response = get(client, url);
       	JSONArray array = new JSONArray(response.getPayload());

       	Tree tree = new Tree();
       	for (int i=0; i< array.length(); i++) {
       		JSONObject root = array.getJSONObject(i);
       		
       		// Restaurant
       		JSONObject restau = root.getJSONObject("restaurant");
       		Integer id = restau.getInt("id");
       		String providerName = "elior-" + id;
       		String name = restau.getString("name");
       		JSONObject gps = restau.getJSONObject("gps");
       		double lat = gps.getDouble("lat");
       		double lng = gps.getDouble("lng");
       		String location = lat + ":" + lng;
       		
       		tree.getOrCreateResource(providerName, "admin", "location", location);
       		tree.getOrCreateResource(providerName, "admin", "id", id);
       		tree.getOrCreateResource(providerName, "admin", "friendlyName", name);
       		
       		// Fluidité
       		JSONObject fluidite = root.getJSONObject("fluidite");
       		if (fluidite.get("data") != null) {
       			JSONObject data = fluidite.getJSONObject("data");
           		tree.getOrCreateResource(providerName, "fluidite", "capacite", data.getInt("capacite"));
           		tree.getOrCreateResource(providerName, "fluidite", "dateDebut", data.getString("dateDebut"));
           		tree.getOrCreateResource(providerName, "fluidite", "dateFin", data.getString("dateFin"));
       		}
       		
       		// Menus
       		JSONObject menus = root.getJSONObject("menus");
       		if (menus.get("data") != null) {
       			tree.getOrCreateResource(providerName, "menus", "menus", menus.getString("data"));
       		}
       	}
       	return tree;
	}
}
