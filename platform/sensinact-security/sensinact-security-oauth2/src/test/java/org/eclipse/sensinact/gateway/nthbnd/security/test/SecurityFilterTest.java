package org.eclipse.sensinact.gateway.nthbnd.security.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.testsuite.KeycloakServer;
import org.junit.Ignore;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;

import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.util.crypto.Base64;

public class SecurityFilterTest extends MidOSGiTest {
	
	class KServer {
		
		KeycloakServer server = null;		
		KServer() throws Throwable {	
		    server = KeycloakServer.bootstrapKeycloakServer(
		    	new String[] {"-b","localhost","-p","8080"});		
			assertNotNull(server);		
			RealmRepresentation representation = KeycloakServer.loadJson(
			    new FileInputStream("src/test/resources/testRealm.json"), 
				    RealmRepresentation.class);
			KeycloakSession session = server.getSessionFactory().create();
	        session.getTransactionManager().begin();
	        try {
	            RealmManager manager = new RealmManager(session);
	            assertNull(manager.getRealmByName("test"));
	            manager.setContextPath(representation.getClients().get(0).getBaseUrl());
	            RealmModel realm = manager.importRealm(representation);
	            System.out.println("Imported realm " + realm.getName());
	            session.getTransactionManager().commit();
	        } finally {
	            session.close();
	        }
	        
//			Keycloak kc = Keycloak.getInstance("http://localhost:8080/auth", 
//			    "test", "testRealmAdmin", "testRealmAdminPassword", "testClient", "testClient");
//			CredentialRepresentation credential = new CredentialRepresentation();
//			credential.setType(CredentialRepresentation.PASSWORD);
//			credential.setValue("test123");
//			UserRepresentation user = new UserRepresentation();
//			user.setUsername("testuser");
//			user.setFirstName("Test");
//			user.setLastName("User");
//			user.setCredentials(Arrays.asList(credential));
//			kc.realm("test").users().create(user);
//			user.setEnabled(true);		
		}
		
		
		public void stop() {
			server.stop();
		}
	}
	
    KServer s;
    
    /**
     * @throws MalformedURLException
     * @throws IOException
     */
    public SecurityFilterTest() throws Exception {
        super();
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#isExcluded(java.lang.String)
     */
    public boolean isExcluded(String fileName) {
        if ("org.apache.felix.framework.security.jar".equals(fileName)) {
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see MidOSGiTest#doInit(java.util.Map)
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doInit(Map configuration) {
    	    	
		try {
			s = new KServer();
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		assertNotNull(s);
		configuration.put("org.eclipse.sensinact.security.oauth2.config", 
    		"src/test/resources/sensinact-security-oauth2.config");
		
		configuration.put("felix.auto.start.1",  
                "file:target/felix/bundle/org.osgi.service.component.jar "+  
                "file:target/felix/bundle/org.osgi.service.cm.jar "+  
                "file:target/felix/bundle/org.osgi.service.metatype.jar "+  
                "file:target/felix/bundle/org.osgi.namespace.extender.jar "+  
                "file:target/felix/bundle/org.osgi.util.promise.jar "+  
                "file:target/felix/bundle/org.osgi.util.function.jar "+  
                "file:target/felix/bundle/org.osgi.service.log.jar "  +
                "file:target/felix/bundle/org.apache.felix.log.jar " + 
                "file:target/felix/bundle/org.apache.felix.scr.jar " +
        		"file:target/felix/bundle/org.apache.felix.fileinstall.jar " +
        		"file:target/felix/bundle/org.apache.felix.configadmin.jar " + 
        		"file:target/felix/bundle/org.apache.felix.framework.security.jar ");
        configuration.put("felix.auto.install.2",  
        	    "file:target/felix/bundle/org.eclipse.paho.client.mqttv3.jar " + 
                "file:target/felix/bundle/mqtt-utils.jar " + 
        	    "file:target/felix/bundle/sensinact-utils.jar " + 
                "file:target/felix/bundle/sensinact-common.jar " + 
        	    "file:target/felix/bundle/sensinact-datastore-api.jar " + 
                "file:target/felix/bundle/sensinact-security-none.jar " + 
                "file:target/felix/bundle/sensinact-generic.jar " + 
                "file:target/felix/bundle/slf4j-api.jar " + 
                "file:target/felix/bundle/slf4j-simple.jar");
        configuration.put("felix.auto.start.2", 
        		"file:target/felix/bundle/sensinact-signature-validator.jar " + 
        		"file:target/felix/bundle/sensinact-core.jar ");
        configuration.put("felix.auto.start.3", 
        		"file:target/felix/bundle/org.apache.felix.http.servlet-api.jar " + 
                "file:target/felix/bundle/org.apache.felix.http.jetty.jar " + 
        		"file:target/felix/bundle/http.jar " +
                "file:target/felix/bundle/dynamicBundle.jar "+
        		"file:target/felix/bundle/sensinact-northbound-access.jar " + 
                "file:target/felix/bundle/rest-access.jar");
        configuration.put("felix.auto.start.4",  
                "file:target/felix/bundle/slider.jar " + 
        		"file:target/felix/bundle/light.jar ");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
        configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");
        configuration.put("org.eclipse.sensinact.http.corsheader", false);        

        configuration.put("org.eclipse.sensinact.gateway.location.latitude", "45.2d");
        configuration.put("org.eclipse.sensinact.gateway.location.longitude", "5.7d");

        configuration.put("felix.log.level","4");
        configuration.put("org.osgi.service.http.port", "8899");
        configuration.put("org.apache.felix.http.debug", true); 
        configuration.put("org.apache.felix.http.jettyEnabled", true);
        configuration.put("org.apache.felix.http.whiteboardEnabled", true);
    }

    @Ignore
	@Test
	public void testKeycloakServer() throws Throwable {//		
//		Manually call http://localhost:8899/sensinact/slider with both adminTester 
//		and anonymousTester users to test oAuth2 security handling	 
//		wait 10mns 	   
		Thread.sleep(600000);
		s.stop();
		Thread.sleep(2000);
	}

	//@Ignore
	@Test
	public void testAccess() throws Throwable {	

		Thread.sleep(2000);
		
		String credentials = new String("anonymousTester:anonymousTester");
		String basic = Base64.encodeBytes(credentials.getBytes(StandardCharsets.UTF_8));
		
		ConnectionConfigurationImpl conf = new ConnectionConfigurationImpl();
		conf.setHttpMethod("GET");
		conf.setAccept("application/json");
		conf.addHeader("Authorization", "Basic " + basic);
		conf.setUri("http://localhost:8899/sensinact/slider");
		
		SimpleResponse response = new SimpleRequest(conf).send();
		assertEquals(401, response.getStatusCode());
		
		credentials = new String("adminTester:adminTester");
		basic = Base64.encodeBytes(credentials.getBytes(StandardCharsets.UTF_8));
		
		conf = new ConnectionConfigurationImpl();
		conf.setHttpMethod("GET");
		conf.setAccept("application/json");
		conf.addHeader("Authorization", "Basic " + basic);
		conf.setUri("http://localhost:8899/sensinact/slider");
		
		response = new SimpleRequest(conf).send();
		assertEquals(200, response.getStatusCode());	

		credentials = new String("unknown:unknown");
		basic = Base64.encodeBytes(credentials.getBytes(StandardCharsets.UTF_8));
		
		conf = new ConnectionConfigurationImpl();
		conf.setHttpMethod("GET");
		conf.setAccept("application/json");
		conf.addHeader("Authorization", "Basic " + basic);
		conf.setUri("http://localhost:8899/sensinact/slider");
		
		response = new SimpleRequest(conf).send();
		assertEquals(401, response.getStatusCode());

		Thread.sleep(5000);
		s.stop();
	}
    
}
