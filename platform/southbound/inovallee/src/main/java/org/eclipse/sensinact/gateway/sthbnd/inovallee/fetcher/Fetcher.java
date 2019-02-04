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

import org.eclipse.sensinact.gateway.sthbnd.inovallee.http.BasicHttpClient;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Fetcher {

	private static final Logger LOG = LoggerFactory.getLogger(Fetcher.class);

	protected Response get(BasicHttpClient client, String url) throws IOException {
		Response response = null;
		try {
			response = client.get(url);
		} catch (Exception e) {
			String msg = "Can't fetch " + url + " : " + e.getMessage(); 
			LOG.error(msg);
			throw new IOException(msg);
		}
       	if (! response.isHttp2XX())
       		throw new IOException("http " + response.getHttpCode() + " when fetching " + url);
       	return response;
	}
}
