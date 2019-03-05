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

public class MobilityFetcher extends Fetcher {

	private final BasicHttpClient client = new BasicHttpClient();
	private final String urlStations = "http://193.48.18.251:8095/mobilite/stations";
	//private final String urlStations = "http://localhost:8095/mobilite/stations";
	
	public Tree fetch() throws IOException {
		Response response = get(client, urlStations);
		JSONArray array = new JSONArray(response.getPayload());

		Tree tree = new Tree();
		for (int i = 0; i < array.length(); i++) {
			JSONObject root = array.getJSONObject(i);

			String id = root.getString("id");
			String code = root.getString("code");
			String libelle = root.getString("libelle");
			String commune = root.getString("commune");

			JSONObject gps = root.getJSONObject("gps");
			double lat = gps.getDouble("lat");
			double lng = gps.getDouble("lng");
			String location = lat + ":" + lng;

			String type = root.getString("type");

			String providerName = "stop-" + code;

			if (type.equals("arret") && id.startsWith("SEM")) {
				tree.getOrCreateResource(providerName, "admin", "location", location);
				tree.getOrCreateResource(providerName, "admin", "id", id);
				tree.getOrCreateResource(providerName, "admin", "code", code);
				tree.getOrCreateResource(providerName, "info", "libelle", libelle);
				tree.getOrCreateResource(providerName, "info", "commune", commune);
				tree.getOrCreateResource(providerName, "info", "type", type);
			}
		}

		return tree;
	}
}
