/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.test;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.AnonymousSession;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.core.security.test.api.MailAccountConnectorMailReplacement;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TestUserManager {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	
	public static int HTTP_PORT = 8899;
    public static String HTTP_ROOTURL = "http://127.0.0.1:" + HTTP_PORT;

    public static String newRequest(String url, String content, String method) {
        SimpleResponse response;
        ConnectionConfigurationImpl<SimpleResponse, SimpleRequest> builder = new ConnectionConfigurationImpl<SimpleResponse, SimpleRequest>();
        builder.setUri(url);
        try {
            if (method.equals("GET")) {
                builder.setHttpMethod("GET");

            } else if (method.equals("POST")) {
                builder.setContentType("application/json");
                builder.setHttpMethod("POST");
                if (content != null && content.length() > 0) {
                    JSONObject jsonData = new JSONObject(content);
                    builder.setContent(jsonData.toString());
                }
            } else {
                return null;
            }
            builder.setAccept("application/json");
            SimpleRequest request = new SimpleRequest(builder);
            response = request.send();
            
            System.out.println(response);
            byte[] responseContent = response.getContent();
            String contentStr = (responseContent == null ? null : new String(responseContent));
            return contentStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    Method getDescription = null;
    Method getMethod = null;

    private Mediator mediator;
    private DataStoreService dataStoreService;
	private File tempDB;
    
	@BeforeEach
	public void before(@InjectBundleContext BundleContext context) throws UnableToFindDataStoreException, UnableToConnectToDataStoreException, IOException {

		tempDB = File.createTempFile("test", ".sqlite");
		tempDB.deleteOnExit();
		
		Path copied = tempDB.toPath();
	    Path originalPath = Paths.get(context.getProperty("sqlitedb"));
	    Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
		
		mediator = new Mediator(context);
		mediator.setProperty("org.eclipse.sensinact.gateway.security.database", tempDB.getAbsolutePath());
		dataStoreService = new SQLiteDataStoreService(mediator);
	}
	
	@AfterEach
	public void after() {
		if(tempDB != null) {
			tempDB.delete();
		}
	}

	protected void doInit(Map configuration) {

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

		configuration.put("org.eclipse.sensinact.gateway.security.database", new File("../sensinact-security-core/src/test/resources/sensinact.sqlite").getAbsolutePath());

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
	    	"file:target/felix/bundle/sensinact-utils.jar "+ 
	    	"file:target/felix/bundle/sensinact-datastore-api.jar "+
	    	"file:target/felix/bundle/sensinact-sqlite-connector.jar "+
	    	"file:target/felix/bundle/sensinact-common.jar "+
	    	"file:target/felix/bundle/sensinact-security-keybuilder.jar "+	
	    	"file:target/felix/bundle/sensinact-security-core.jar "+
			"file:target/felix/bundle/http.jar "  +
	    	"file:target/felix/bundle/sensinact-framework-extension.jar "+
	    	"file:target/felix/bundle/slf4j-simple.jar");

	    configuration.put("felix.auto.start.2", 
	    	"file:target/felix/bundle/sensinact-test-configuration.jar "+
	    	"file:target/felix/bundle/sensinact-signature-validator.jar " +
	    	"file:target/felix/bundle/org.apache.felix.http.servlet-api.jar " +
	    	"file:target/felix/bundle/org.apache.felix.http.api.jar " +
	    	"file:target/felix/bundle/org.apache.felix.http.jetty.jar " +
	    	"file:target/felix/bundle/org.apache.aries.javax.jax.rs-api.jar "+
			"file:target/felix/bundle/http-tools.jar " );

		configuration.put("felix.auto.start.3",
				"file:target/felix/bundle/sensinact-core.jar " + 
				"file:target/felix/bundle/sensinact-generic.jar " +
				"file:target/felix/bundle/sensinact-northbound-access.jar "+
				"file:target/felix/bundle/rest-access.jar ");

		configuration.put("felix.auto.start.4", 
				"file:target/felix/bundle/slider.jar " + 
				"file:target/felix/bundle/fan.jar " + 
				"file:target/felix/bundle/button.jar " );
		
        configuration.put("org.eclipse.sensinact.gateway.location.latitude", "45.2d");
        configuration.put("org.eclipse.sensinact.gateway.location.longitude", "5.7d");
		
		configuration.put("mail.account.connector.host","smtp.server.fr");
		configuration.put("mail.account.connector.port","465");
		configuration.put("mail.account.connector.from","sensinact@bigclout.eu");
		configuration.put("mail.account.connector.login","cmunilla@server.fr");
		configuration.put("mail.account.connector.password","478569LM");

		configuration.put("org.osgi.service.http.host","localhost");
		configuration.put("org.osgi.service.http.port","8899");
		configuration.put("org.apache.felix.http.enable","true");
	}

	@Test
	@Disabled
	public void testUserManager(@InjectInstalledBundle(value = "tb.jar", start = true) Bundle extra, 
			@InjectService Core core,
			@InjectService(cardinality = 0) ServiceAware<MailAccountConnectorMailReplacement> replacerServiceAware) throws Exception {
		
		UserDAO dao = new UserDAO(mediator, dataStoreService);
		String encryptedPassword = CryptoUtils.cryptWithMD5("mytestpassword");
		UserEntity entity = dao.find("mytester", encryptedPassword);
		assertNull(entity);
		
		AnonymousSession as = core.getAnonymousSession();
		assertNotNull(as);
		as.registerUser("mytester", encryptedPassword, "fake@test.fr", "MAIL");
		Thread.sleep(2000);
		
		MailAccountConnectorMailReplacement replacer = replacerServiceAware.waitForService(500); 
		
		String message = replacer.getMailDetails();
		
		String link = new StringBuilder().append("http").append(message.split("http")[1]).toString();
		String validation = newRequest(link,null,"GET");
		
		entity = dao.find("mytester", encryptedPassword);
		assertNotNull(entity);	
		
		String publicKey = validation.substring(validation.lastIndexOf(':')+2);
		System.out.println(publicKey);
		entity = dao.find(publicKey);
		assertNotNull(entity);	
		
		dao.delete(entity);
		
		entity = dao.find("mytester", encryptedPassword);
		assertNull(entity);
	}
}