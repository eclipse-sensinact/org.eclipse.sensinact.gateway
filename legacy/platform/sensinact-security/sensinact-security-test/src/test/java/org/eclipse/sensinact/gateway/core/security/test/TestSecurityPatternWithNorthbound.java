/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.core.security.http.test.HttpServiceTestClient;
import org.eclipse.sensinact.gateway.core.security.ws.test.WsServiceTestClient;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(ServiceExtension.class)
@Disabled
public class TestSecurityPatternWithNorthbound{
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ProvidersList {

		@JsonProperty(value="statusCode")
		private int statusCode;
		
		@JsonProperty(value="type")
		private String type;
		
		@JsonProperty(value="uri")
		private String uri;
		
		@JsonProperty(value="providers")
		private List<String> providers;
	
		/**
		 * Constructor 
		 */
		public ProvidersList() {}
	
		/**
		 * Constructor 
		 */
		public ProvidersList(int statusCode, String type, String uri, List<String> providers){
			this.statusCode = statusCode;
			this.type = type;
			this.uri = uri;
			if(providers != null)
				this.providers = new ArrayList<String>(providers);
			else 
				this.providers = Collections.emptyList();
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * @param statusCode the statusCode to set
		 */
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the uri
		 */
		public String getUri() {
			return uri;
		}

		/**
		 * @param uri the uri to set
		 */
		public void setUri(String uri) {
			this.uri = uri;
		}

		/**
		 * @return the providers
		 */
		public List<String> getProviders() {
			return providers;
		}

		/**
		 * @param providers the providers to set
		 */
		public void setProviders(List<String> providers) {
			this.providers = providers;
		}
	
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ServicesList {

		@JsonProperty(value="statusCode")
		private int statusCode;
		
		@JsonProperty(value="type")
		private String type;
		
		@JsonProperty(value="uri")
		private String uri;
		
		@JsonProperty(value="services")
		private List<String> services;
	
		/**
		 * Constructor 
		 */
		public ServicesList() {}
	
		/**
		 * Constructor 
		 */
		public ServicesList(int statusCode, String type, String uri, List<String> services){
			this.statusCode = statusCode;
			this.type = type;
			this.uri = uri;
			if(services != null)
				this.services = new ArrayList<String>(services);
			else 
				this.services = Collections.emptyList();
		}

		/**
		 * @return the statusCode
		 */
		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * @param statusCode the statusCode to set
		 */
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @param type the type to set
		 */
		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return the uri
		 */
		public String getUri() {
			return uri;
		}

		/**
		 * @param uri the uri to set
		 */
		public void setUri(String uri) {
			this.uri = uri;
		}

		/**
		 * @return the services
		 */
		public List<String> getServices() {
			return services;
		}

		/**
		 * @param services the providers to set
		 */
		public void setProviders(List<String> services) {
			this.services = services;
		}
	
	}
	/**
	 * LoginResponse wraps sensiNact Session token
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class LoginResponse {
	
		@JsonProperty(value="validUntil")
		private long validUntil;
		
		@JsonProperty(value="generated")
		private long generated;
		
		@JsonProperty(value="token")
		private String token;
		
	
		/**
		 * Constructor 
		 */
		public LoginResponse() {}
	
		/**
		 * Constructor 
		 * 
		 * @param validUntil the long timestamp of the expiration time
		 * @param generated the long timestamp of the generation time
		 * @param token the String access token 
		 */
		public LoginResponse(long validUntil,long generated, String token){
			this.validUntil = validUntil;
			this.generated = generated;
			this.token = token;
		}

		/**
		 * @return the validUntil
		 */
		public long getValidUntil() {
			return validUntil;
		}

		/**
		 * @param validUntil the validUntil to set
		 */
		public void setValidUntil(long validUntil) {
			this.validUntil = validUntil;
		}

		/**
		 * @return the token
		 */
		public String getToken() {
			return token;
		}

		/**
		 * @param token the token to set
		 */
		public void setToken(String token) {
			this.token = token;
		}

		/**
		 * @return the generated
		 */
		public long getGenerated() {
			return generated;
		}

		/**
		 * @param generated the generated to set
		 */
		public void setGenerated(long generated) {
			this.generated = generated;
		}
	}
	
	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	private static final String SLIDERS_DEFAULT = "[\"slider01\",\"slider02\",\"slider11\"]";
	private static final String SLIDERS_PROP = "org.eclipse.sensinact.simulated.sliders";
	private static final String GUI_ENABLED = "org.eclipse.sensinact.simulated.gui.enabled";

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	Method getDescription = null;
	public TestSecurityPatternWithNorthbound() throws Exception {
		super();
		getDescription = Describable.class.getDeclaredMethod("getDescription");
	}

	public boolean isExcluded(String fileName) {
		switch(fileName) {
		case "org.apache.felix.framework.security.jar":
			return true;
		default:
			break;
		}
		return false;
	}

	protected void doInit(Map<String, Comparable> configuration) {
		configuration.put("org.osgi.framework.system.packages.extra",
			"org.eclipse.sensinact.gateway.test," + 
			"com.sun.net.httpserver," + 
			"javax.mail," + 
			"javax.mail.internet," + 
			"javax.microedition.io," +
			"javax.management.modelmbean," + 
			"javax.management.remote,"	+
			"javax.persistence," +
			"junit.framework," + 
			"junit.textui," + 
			"org.w3c.dom," + 
			"org.xml.sax," + 
			"org.xml.sax.helpers," + 
			"sun.misc,"+ 
			"sun.security.action");

		configuration.put("org.eclipse.sensinact.simulated.gui.enabled", "false");

		configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
		configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

		configuration.put("org.eclipse.sensinact.gateway.security.database", 
				new File("../sensinact-security-core/src/test/resources/sensinact.sqlite").getAbsolutePath());

    	configuration.put("felix.auto.start.1",  
           "file:target/felix/bundle/org.osgi.service.component.jar "+ 
           "file:target/felix/bundle/org.osgi.service.cm.jar "+  
           "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
           "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
           "file:target/felix/bundle/org.osgi.util.promise.jar "+  
           "file:target/felix/bundle/org.osgi.util.function.jar "+  
           "file:target/felix/bundle/org.osgi.util.pushstream.jar "+
           "file:target/felix/bundle/org.osgi.service.log.jar "  +
           "file:target/felix/bundle/org.apache.felix.log.jar " + 
           "file:target/felix/bundle/org.apache.felix.scr.jar " +
           "file:target/felix/bundle/org.apache.felix.fileinstall.jar " +
           "file:target/felix/bundle/org.apache.felix.configadmin.jar " + 
           "file:target/felix/bundle/org.apache.felix.framework.security.jar ");
    	
        configuration.put("felix.auto.install.2",  
    	    "file:target/felix/bundle/slf4j-api.jar "+
    		"file:target/felix/bundle/http.jar "+ 
    		"file:target/felix/bundle/sensinact-utils.jar "+ 
			"file:target/felix/bundle/sensinact-datastore-api.jar "+
			"file:target/felix/bundle/sensinact-sqlite-connector.jar "+
			"file:target/felix/bundle/sensinact-common.jar "+
			"file:target/felix/bundle/sensinact-framework-extension.jar "+
			"file:target/felix/bundle/sensinact-northbound-access.jar " +
	    	"file:target/felix/bundle/sensinact-security-keybuilder.jar "+	
			"file:target/felix/bundle/sensinact-security-core.jar "+
	    	"file:target/felix/bundle/slf4j-simple.jar");

		configuration.put("felix.auto.start.2", 
			"file:target/felix/bundle/sensinact-test-configuration.jar "+
			"file:target/felix/bundle/sensinact-signature-validator.jar " +
			"file:target/felix/bundle/org.apache.felix.http.servlet-api.jar " +
			"file:target/felix/bundle/org.apache.felix.http.api.jar " +
			"file:target/felix/bundle/org.apache.felix.http.jetty.jar " +
			"file:target/felix/bundle/org.apache.aries.javax.jax.rs-api.jar");

		configuration.put("felix.auto.start.3",
			"file:target/felix/bundle/sensinact-core.jar " + 
		    "file:target/felix/bundle/sensinact-generic.jar " + 
			"file:target/felix/bundle/rest-access.jar ");

		configuration.put("felix.auto.start.4", "file:target/felix/bundle/slider.jar ");       
		configuration.put(SLIDERS_PROP, SLIDERS_DEFAULT);

        configuration.put("org.osgi.service.http.port", "8899");
        configuration.put("org.apache.felix.http.jettyEnabled", true);
        configuration.put("org.apache.felix.http.whiteboardEnabled", true);
        
		configuration.put("felix.log.level", "4");
	}

	@Test
	public void testSecurityPatternWithHttpNorthbound(@InjectBundleContext BundleContext context) throws Throwable {
		// slider[0-9]{2} - authenticated access level
		// slider[0-9]{2}/admin - admin authenticated access level
		// cea user is admin on slider[0-9]{2}

		// slider0[0-9] - authenticated access level
		// slider0[0-9]/cursor - authenticated access level
		// fake user is authenticated on slider0[0-9]

		// slider1[0-9] - authenticated access level
		// slider1[0-9]/cursor - authenticated access level
		// fake2 user is authenticated on slider1[0-9]

    	Mediator mediator = new Mediator(context);
		String response = HttpServiceTestClient.newRequest(mediator, "http://localhost:8899/sensinact/providers", 
				null, "GET", null);
		System.out.println("====================================>>>>>");
		System.out.println(response);
		System.out.println("====================================>>>>>");

		ObjectMapper mapper = new ObjectMapper();
		ProvidersList providers = mapper.readValue(response, ProvidersList.class);
		assertTrue(providers.getProviders().isEmpty());

		// ******************************************************
		// admin
		// the admin user is suppose to see every thing
		// service providers and services
		
		String sessionToken = this.snaHttpAuthenticate("cea", "sensiNact_team");
		response = HttpServiceTestClient.newRequest(mediator, "http://localhost:8899/sensinact/providers", 
				null, "GET", sessionToken);
		//"{\"statusCode\":200,\"providers\":[\"slider01\",\"slider02\",\"slider11\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}"
		System.out.println("====================================>>>>>");
		System.out.println(response);

		providers = mapper.readValue(response, ProvidersList.class);
		assertEquals(3,providers.getProviders().size());

		Iterator<String> iterator = providers.getProviders().iterator();

		while (iterator.hasNext()) {
			String provider = iterator.next();
			response = HttpServiceTestClient.newRequest(mediator, 
				String.format("http://localhost:8899/sensinact/providers/%s/services",provider), 
				null, "GET", sessionToken);
			System.out.println("\t" + response);		

			ServicesList services = mapper.readValue(response, ServicesList.class);
			assertEquals(2,services.getServices().size());
		}
		
		System.out.println("====================================>>>>>");		

		// *************************************
		// fake
		// the fake user is suppose to see only two service providers
		// and only the cursor service for each one
		
		sessionToken = this.snaHttpAuthenticate("fake", "fake");
		response = HttpServiceTestClient.newRequest(mediator, "http://localhost:8899/sensinact/providers", 
				null, "GET", sessionToken);
		//"{\"statusCode\":200,\"providers\":[\"slider01\",\"slider02\"]," + "\"type\":\"PROVIDERS_LIST\",\"uri\":\"/\"}"
		System.out.println("====================================>>>>>");
		System.out.println(response);

		providers = mapper.readValue(response, ProvidersList.class);
		assertEquals(2,providers.getProviders().size());

		iterator = providers.getProviders().iterator();

		while (iterator.hasNext()) {
			String provider = iterator.next();
			response = HttpServiceTestClient.newRequest(mediator, 
				String.format("http://localhost:8899/sensinact/providers/%s/services",provider), 
				null, "GET", sessionToken);
			System.out.println("\t" + response);
			ServicesList services = mapper.readValue(response, ServicesList.class);
			assertEquals(1,services.getServices().size());
		}
		
		System.out.println("====================================>>>>>");

		// ***************************************
		// fake2
		// the fake2 user is suppose to see only one service provider
		// and only its cursor service
		sessionToken = this.snaHttpAuthenticate("fake2", "fake2");
		response = HttpServiceTestClient.newRequest(mediator, "http://localhost:8899/sensinact/providers", 
				null, "GET", sessionToken);

		System.out.println("====================================>>>>>");
		System.out.println(response);

		mapper = new ObjectMapper();
		providers = mapper.readValue(response, ProvidersList.class);
		assertEquals(1,providers.getProviders().size());

		iterator = providers.getProviders().iterator();

		while (iterator.hasNext()) {
			String provider = iterator.next();
			response = HttpServiceTestClient.newRequest(mediator, 
				String.format("http://localhost:8899/sensinact/providers/%s/services",provider), 
				null, "GET", sessionToken);
			System.out.println("\t" + response);			
			mapper = new ObjectMapper();
			ServicesList services = mapper.readValue(response, ServicesList.class);
			assertEquals(1,services.getServices().size());
		}		
		System.out.println("====================================>>>>>");
	}	

	@Test
	public void testSecurityPatternWithWsNorthbound(@InjectBundleContext BundleContext context) throws Throwable {
		// slider[0-9]{2} - authenticated access level
		// slider[0-9]{2}/admin - admin authenticated access level
		// cea user is admin on slider[0-9]{2}

		// slider0[0-9] - authenticated access level
		// slider0[0-9]/cursor - authenticated access level
		// fake user is authenticated on slider0[0-9]

		// slider1[0-9] - authenticated access level
		// slider1[0-9]/cursor - authenticated access level
		// fake2 user is authenticated on slider1[0-9]

    	Mediator mediator = new Mediator(context);

    	WsServiceTestClient client = new WsServiceTestClient(null);
        new Thread(client).start();
        
		String response = this.synchronizedRequest(client, "/sensinact/providers", null);
		System.out.println("====================================>>>>>");
		System.out.println(response);
		System.out.println("====================================>>>>>");

		ObjectMapper mapper = new ObjectMapper();
		ProvidersList providers = mapper.readValue(response, ProvidersList.class);
		assertTrue(providers.getProviders().isEmpty());

		// ******************************************************
		// admin
		// the admin user is suppose to see every thing
		// service providers and services
		//Retrieve connection information from sensiNact
		
    	String token = Base64.getEncoder().encodeToString(String.format("%s:%s","cea","sensiNact_team").getBytes());    	
    	client = new WsServiceTestClient("Basic "+token);
        new Thread(client).start();
        
		response = this.synchronizedRequest(client, "/sensinact/providers", null);
		System.out.println("====================================>>>>>");
		System.out.println(response);

		providers = mapper.readValue(response, ProvidersList.class);
		assertEquals(3,providers.getProviders().size());
		
		Iterator<String> iterator = providers.getProviders().iterator();

		while (iterator.hasNext()) {
			String provider = iterator.next();
			response = this.synchronizedRequest(client, String.format("/sensinact/providers/%s/services",provider),null);
			System.out.println("\t" + response);	
			ServicesList services = mapper.readValue(response, ServicesList.class);
			assertEquals(2,services.getServices().size());
		}
		
		System.out.println("====================================>>>>>");		

		// *************************************
		// fake
		// the fake user is suppose to see only two service providers
		// and only the cursor service for each one

    	token = Base64.getEncoder().encodeToString(String.format("%s:%s","fake","fake").getBytes());    	
    	client = new WsServiceTestClient("Basic "+token);    
        new Thread(client).start();	

		response = this.synchronizedRequest(client, "/sensinact/providers",null);

		System.out.println("====================================>>>>>");
		System.out.println(response);

		providers = mapper.readValue(response, ProvidersList.class);
		assertEquals(2,providers.getProviders().size());
		
		iterator = providers.getProviders().iterator();

		while (iterator.hasNext()) {
			String provider = iterator.next();
			response = this.synchronizedRequest(client, String.format("/sensinact/providers/%s/services",provider),null);
			System.out.println("\t" + response);	
			ServicesList services = mapper.readValue(response, ServicesList.class);
			assertEquals(1,services.getServices().size());
		}
		
		System.out.println("====================================>>>>>");
		
		// ***************************************
		// fake2
		// the fake2 user is suppose to see only one service provider
		// and only its cursor service
		token = Base64.getEncoder().encodeToString(String.format("%s:%s","fake2","fake2").getBytes());    	
    	client = new WsServiceTestClient("Basic "+token);
        new Thread(client).start();
        
		response = this.synchronizedRequest(client, "/sensinact/providers",null);
		System.out.println("====================================>>>>>");
		System.out.println(response);

		mapper = new ObjectMapper();
		providers = mapper.readValue(response, ProvidersList.class);
		assertEquals(1,providers.getProviders().size());
		
		iterator = providers.getProviders().iterator();

		while (iterator.hasNext()) {
			String provider = iterator.next();
			response = this.synchronizedRequest(client, String.format("/sensinact/providers/%s/services",provider),null);
			System.out.println("\t" + response);
			ServicesList services = mapper.readValue(response, ServicesList.class);
			assertEquals(1,services.getServices().size());
		}
		System.out.println("====================================>>>>>");
	}
	
	private String synchronizedRequest(WsServiceTestClient client, String url, String content) {
        String simulated = null;
        long wait = 10000;
        client.newRequest(url, content);

        while (!client.isAvailable() && wait > 0) {
            wait-=100;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
        if (client.isAvailable()) {
            simulated = client.getResponseMessage();
        }
        return simulated;
    }
	
    private String snaHttpAuthenticate(String username, String password) throws IOException {
    	//Retrieve connection information from sensiNact
    	String token = Base64.getEncoder().encodeToString(String.format("%s:%s",username,password).getBytes());

		ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> connection = 
			new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
		
		connection.setUri("http://localhost:8899/sensinact.login");
		connection.setAccept("application/json");
		connection.addHeader("Authorization", "Basic " + token);
		connection.setHttpMethod("GET");
		
		SimpleRequest req = new SimpleRequest(connection);
		SimpleResponse resp = req.send();

		String response = new String(resp.getContent());
		
		ObjectMapper mapper = new ObjectMapper();
		LoginResponse loginResponse = mapper.readValue(response,LoginResponse.class);
		return loginResponse.getToken();
    }

}
