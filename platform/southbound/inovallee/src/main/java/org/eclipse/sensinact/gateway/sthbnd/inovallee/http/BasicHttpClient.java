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
package org.eclipse.sensinact.gateway.sthbnd.inovallee.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(BasicHttpClient.class);

	public Response get(String url) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		// add request header
		// con.setRequestProperty("key", "value");

		int httpCode = con.getResponseCode();

		StringBuilder response = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
		} catch (Exception e) {
			LOG.error("Can't parse payload", e);
		}

		return new Response(httpCode, response.toString());
	}
}
