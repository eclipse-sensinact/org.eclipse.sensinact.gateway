/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.tools.swagger;

import java.net.HttpURLConnection;
import java.net.URL;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SwaggerTest {

	@ParameterizedTest
	@ValueSource(strings = { "/swagger-api", "/swagger-api/" })
	public void testRedirect(String path) throws Exception {
		URL url = new URL("http://localhost:8080" + path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("GET");
		connection.connect();

		Assertions.assertThat(connection.getResponseCode()).isEqualTo(302);
		Assertions.assertThat(connection.getHeaderField("location"))
				.isEqualTo("http://localhost:8080/swagger-api/index.html");
	}

	@ParameterizedTest
	@ValueSource(strings = { "/swagger-api/index.html", "/swagger-api/rest-api-swagger.yaml",
			"/swagger-api/schemas/act_request.json" })
	public void testIndex(String text) throws Exception {
		URL url = new URL("http://localhost:8080" + text);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		Assertions.assertThat(connection.getResponseCode()).isEqualTo(200);

	}

}
