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
package org.eclipse.sensinact.gateway.sthbnd.inovallee;

import java.io.IOException;

import org.eclipse.sensinact.gateway.generic.model.Tree;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.http.BasicHttpClient;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.http.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fetcher {

	private static final Logger LOG = LoggerFactory.getLogger(Fetcher.class);
	
    private final BasicHttpClient client = new BasicHttpClient(); 
	private final String url = "http://193.48.18.251:8095/restaurants/infos";
	
	public Tree fetch() throws IOException {
		Tree tree = new Tree();
		registerRestaurents(tree, client, url);       	
       	return tree;
	}

	
	private static void registerRestaurents(Tree tree, BasicHttpClient client, String url) throws IOException {
       	Response response = client.get(url);
       	if (! response.isHttp2XX())
       		throw new IOException("http " + response.getHttpCode() + " when fetching " + url);
       	JSONArray array = new JSONArray(response.getPayload());
       	for (int i=0; i< array.length(); i++) {
       		JSONObject restau = array.getJSONObject(i);
       		Integer id = restau.getInt("id");
       		String providerName = "elior-" + id;
       		String name = restau.getString("name");
       		JSONObject gps = restau.getJSONObject("gps");
       		double lat = gps.getDouble("lat");
       		double lng = gps.getDouble("lng");
       		String location = lat + ":" + lng;
       		
       		tree.getOrCreateResource(providerName, "admin", "location", location);
       		tree.getOrCreateResource(providerName, "admin", "id", id);
       		tree.getOrCreateResource(providerName, "admin", "name", name);
       	}
	}
}
