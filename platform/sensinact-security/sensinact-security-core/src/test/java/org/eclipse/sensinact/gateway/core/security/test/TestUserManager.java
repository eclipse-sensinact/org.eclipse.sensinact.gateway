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
package org.eclipse.sensinact.gateway.core.security.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.AnonymousSession;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.InvalidServiceProviderException;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.test.MidOSGiTest;
import org.eclipse.sensinact.gateway.test.MidProxy;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressWarnings({"unchecked", "rawtypes"})
public class TestUserManager extends MidOSGiTest {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	
	public static int HTTP_PORT = 8895;
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
    Method setMethod = null;
    Method actMethod = null;

    private Mediator mediator;
    private DataStoreService dataStoreService;
    
    public TestUserManager() throws Exception {
        super();
        getDescription = Describable.class.getDeclaredMethod("getDescription");
        getMethod = Resource.class.getDeclaredMethod("get", new Class<?>[]{String.class});
        setMethod = Resource.class.getDeclaredMethod("set", new Class<?>[]{String.class, Object.class});
        actMethod = ActionResource.class.getDeclaredMethod("act", new Class<?>[]{Object[].class});
    }

	@Before
	public void before() throws UnableToFindDataStoreException, UnableToConnectToDataStoreException {
		mediator = new Mediator(context);
		mediator.setProperty("org.eclipse.sensinact.gateway.security.database", "src/test/resources/sensinact.sqlite");
		dataStoreService = new SQLiteDataStoreService(mediator);
	}
	
	/**
	 * @inheritDoc
	 *
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
	 *
	 * @see MidOSGiTest#doInit(java.util.Map)
	 */
	@Override
	protected void doInit(Map configuration) {
		configuration.put("org.osgi.framework.system.packages.extra",
		"org.eclipse.sensinact.gateway.test," 
		+ "com.sun.net.httpserver," 
		+ "javax.net.ssl,"
		+ "javax.xml.parsers," 
		+ "javax.imageio," 
		+ "javax.management," 
		+ "javax.naming," 
		+ "javax.sql,"
		+ "javax.swing," 
		+ "javax.swing.border," 
		+ "javax.swing.event," 
		+ "javax.mail,"
		+ "javax.mail.internet," 
		+ "javax.management.modelmbean," 
		+ "javax.management.remote,"
		+ "javax.xml.parsers," 
		+ "javax.security.auth," 
		+ "javax.security.cert," 
		+ "junit.framework,"
		+ "junit.textui," 
		+ "org.w3c.dom," 
		+ "org.xml.sax," 
		+ "org.xml.sax.helpers," 
		+ "sun.misc,"
		+ "sun.security.action");

		configuration.put("org.eclipse.sensinact.simulated.gui.enabled", "false");

		configuration.put("org.eclipse.sensinact.gateway.security.jks.filename", "target/felix/bundle/keystore.jks");
		configuration.put("org.eclipse.sensinact.gateway.security.jks.password", "sensiNact_team");

		configuration.put("org.eclipse.sensinact.gateway.security.database",
				new File("src/test/resources/sensinact.sqlite").getAbsolutePath());

		configuration.put("felix.auto.start.1",
				"file:target/felix/bundle/org.osgi.compendium.jar "
			  + "file:target/felix/bundle/org.apache.felix.framework.security.jar "
			  + "file:target/felix/bundle/org.apache.felix.configadmin.jar "
			  + "file:target/felix/bundle/org.apache.felix.fileinstall.jar");

		configuration.put("felix.auto.install.2",
				"file:target/felix/bundle/sensinact-utils.jar "
			  + "file:target/felix/bundle/sensinact-datastore-api.jar "
			  + "file:target/felix/bundle/sensinact-sqlite-connector.jar "
			  + "file:target/felix/bundle/sensinact-common.jar "
			  + "file:target/felix/bundle/http.jar "  
			  + "file:target/felix/bundle/sensinact-framework-extension.jar "
		      + "file:target/felix/bundle/dynamicBundle.jar");

		configuration.put("felix.auto.start.2", 
				"file:target/felix/bundle/sensinact-test-configuration.jar "
			  + "file:target/felix/bundle/sensinact-signature-validator.jar "
			  + "file:target/felix/bundle/javax.servlet-api.jar "
			  + "file:target/felix/bundle/org.apache.felix.http.api.jar "
			  + "file:target/felix/bundle/org.apache.felix.http.jetty.jar "
			  + "file:target/felix/bundle/http-tools.jar " );

		configuration.put("felix.auto.start.3",
				"file:target/felix/bundle/sensinact-core.jar " + 
				"file:target/felix/bundle/sensinact-generic.jar " +
				"file:target/felix/bundle/sensinact-northbound-access.jar ");

		configuration.put("felix.auto.start.4", 
				"file:target/felix/bundle/rest-access.jar " +
				"file:target/felix/bundle/slider.jar " + 
				"file:target/felix/bundle/fan.jar " + 
				"file:target/felix/bundle/button.jar " );
		
		configuration.put("mail.account.connector.host","smtp.server.fr");
		configuration.put("mail.account.connector.port","465");
		configuration.put("mail.account.connector.from","sensinact@bigclout.eu");
		configuration.put("mail.account.connector.login","cmunilla@server.fr");
		configuration.put("mail.account.connector.password","478569LM");

		configuration.put("org.osgi.service.http.host","localhost");
		configuration.put("org.osgi.service.http.port","8895");
		configuration.put("org.apache.felix.http.enable","true");
	}

	@Test
	public void testUserManager() throws Exception {
		this.initializeMoke(
				new File("./src/test/resources/MANIFEST.MF"), 
				new File("./extra-src/test/java"), 
				new File("./target/extra-test-classes"));
		
		UserDAO dao = new UserDAO(mediator, dataStoreService);
		String encryptedPassword = CryptoUtils.cryptWithMD5("mytestpassword");
		UserEntity entity = dao.find("mytester", encryptedPassword);
		assertNull(entity);
		
		MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
		Core core = mid.buildProxy();		
		AnonymousSession as = core.getAnonymousSession();
		assertNotNull(as);
		as.registerUser("mytester", encryptedPassword, "fake@test.fr", "MAIL");
		Thread.sleep(2000);
		
		ServiceReference<?>[] references = super.getBundleContext().getServiceReferences("org.eclipse.sensinact.gateway.mail.connector.MailAccountConnectorMailReplacement",null);
		Object mailAccountConnectorMailReplacement  = super.getBundleContext().getService(references[0]);		
		Method method = mailAccountConnectorMailReplacement.getClass().getMethod("getMailDetails");
		method.setAccessible(true);
		String message = (String) method.invoke(mailAccountConnectorMailReplacement);
		
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
	
    private void initializeMoke(File manifestFile, File... sourceDirectories) throws Exception {
        File tmpDirectory = new File("./target/felix/tmp");
        new File(tmpDirectory, "dynamicBundle.jar").delete();

        int length = (sourceDirectories == null ? 0 : sourceDirectories.length);
        File[] sources = new File[length];
        int index = 0;
        if (length > 0) {
            for (; index < length; index++) {
                sources[index] = sourceDirectories[index];
            }
        }
        super.createDynamicBundle(manifestFile, tmpDirectory, sources);
        Bundle bundle = super.installDynamicBundle(new File(tmpDirectory, "dynamicBundle.jar").toURI().toURL());

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(super.classloader);
        try {
            bundle.start();

        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        Thread.sleep(10 * 1000);
    }
}