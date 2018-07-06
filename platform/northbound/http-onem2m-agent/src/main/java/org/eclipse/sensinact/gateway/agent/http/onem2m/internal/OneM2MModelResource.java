/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial implementation
 */
package org.eclipse.sensinact.gateway.agent.http.onem2m.internal;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OneM2MModelResource {
	private static Logger LOG = LoggerFactory
			.getLogger(OneM2MModelResource.class.getCanonicalName());
	private final String cseBase;
	private final String provider;
	private Set<String> resources = new HashSet<String>();
	private final String origin;

	public OneM2MModelResource(String provider, String cseBase) {
		this.cseBase = cseBase;
		this.provider = provider;
		this.origin = "CEA" + provider.toUpperCase();
	}

	public void addResourceInfo(String service, String resource, String value) {

		LOG.debug(
				"Integrating reading from provider '{}' resource '{}' with value '{}'. The server is '{}'.",
				provider, resource, value, cseBase);
		final String fullId = String.format("%s/%s", service, resource);
		if (!resources.contains(fullId)) {
			resources.add(fullId);
			JSONObject resourceContainer = new JSONObject();
			JSONObject content = new JSONObject();
			content.put("rn", resource);
			content.put("lbl", new JSONArray().put(provider));
			resourceContainer.put("m2m:cnt", content);

			try {
				Util.createRequest(cseBase, "POST",
						"CEA" + provider.toUpperCase(), "/" + provider,
						"application/json;ty=3", resourceContainer);
			} catch (IOException e) {
				LOG.error(
						"Failed to process http request to OneM2M server {} to update device location",
						cseBase, e);
			}

		}
		JSONObject resourceValueContent = new JSONObject();
		JSONObject content = new JSONObject();
		content.put("con", value.toString());
		resourceValueContent.put("m2m:cin", content);
		try {
			Util.createRequest(cseBase, "POST", "CEA" + provider.toUpperCase(),
					"/" + provider + "/" + resource, "application/json;ty=4",
					resourceValueContent);
		} catch (IOException e) {
			LOG.error(
					"Failed to process http request to OneM2M server {} to update device info",
					cseBase, e);
		}

	}

	private void removeResource(String service, String resource) {
		try {
			// final String fullId=String.format("%s/%s",service,resource);
			JSONObject content = new JSONObject();
			content.put("rn", resource);
			Util.createRequest(cseBase, "DELETE", origin, "/" + provider,
					"application/json;ty=3",
					new JSONObject().put("m2m:cnt", content));

		} catch (IOException e) {
			LOG.debug("Failed to remove resource", e);
		}
	}

	public void removeResource() {
		for (String vl : resources) {
			final String[] splitName = vl.split("/");
			final String service = splitName[0];
			final String resource = splitName[1];
			removeResource(service, resource);
			resources.remove(vl);
		}
	}

}
