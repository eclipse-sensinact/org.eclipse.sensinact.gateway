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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.sensinact.gateway.core.AnonymousSession;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.datastore.api.UnableToConnectToDataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.UnableToFindDataStoreException;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService;
import org.eclipse.sensinact.gateway.datastore.sqlite.SQLiteDataStoreService.SQLiteConfig;
import org.eclipse.sensinact.gateway.mail.connector.api.MailAccountConnectorMailReplacement;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectInstalledBundle;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.context.InstalledBundleExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(InstalledBundleExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class TestUserManager extends AbstractConfiguredSecurityTest {
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

    private SQLiteDataStoreService dataStoreService;
    

    private File tempDB;
    
	@BeforeEach
	public void before(@InjectBundleContext BundleContext context) throws UnableToFindDataStoreException, UnableToConnectToDataStoreException, IOException {

		tempDB = File.createTempFile("test", ".sqlite");
		tempDB.deleteOnExit();
		
		Path copied = tempDB.toPath();
	    Path originalPath = Paths.get(context.getProperty("sqlitedb"));
	    Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
		
		SQLiteConfig sqlLiteConfig = Mockito.mock(SQLiteDataStoreService.SQLiteConfig.class);
		Mockito.when(sqlLiteConfig.database()).thenReturn(tempDB.getAbsolutePath());
		dataStoreService = new SQLiteDataStoreService( );
		dataStoreService.start(sqlLiteConfig);
		
		context.registerService(SecurityDataStoreService.class, dataStoreService, null);
	}
	
	@AfterEach
	public void after() {
		if(tempDB != null) {
			tempDB.delete();
		}
	}

	@Test
	@WithConfiguration(
			pid = "sensinact.mail.account.connector",
			location = "?",
			properties = {
					@Property(key="host", value="smtp.server.fr"),
					@Property(key="port", value="465"),
					@Property(key="from", value = "sensinact@bigclout.eu"),
					@Property(key="login", value="cmunilla@server.fr"),
					@Property(key="password", value="478569LM")
			}
		)
	public void testUserManager(
			@InjectService(timeout = 1000) Core core,
			@InjectInstalledBundle(value = "tb.jar", start = true) Bundle bundle,
			@InjectService(cardinality = 0) ServiceAware<MailAccountConnectorMailReplacement> mailReplacerAware
			) throws Exception {
		
		UserDAO dao = new UserDAO(dataStoreService);
		String encryptedPassword = CryptoUtils.cryptWithMD5("mytestpassword");
		UserEntity entity = dao.find("mytester", encryptedPassword);
		assertNull(entity);
//		
//		MidProxy<Core> mid = new MidProxy<Core>(classloader, this, Core.class);
//		Core core = mid.buildProxy();		
		AnonymousSession as = core.getAnonymousSession();
		assertNotNull(as);
		as.registerUser("mytester", encryptedPassword, "fake@test.fr", "MAIL");
		Thread.sleep(2000);
//		
//		ServiceReference<?>[] references = super.getBundleContext().getServiceReferences("org.eclipse.sensinact.gateway.mail.connector.MailAccountConnectorMailReplacement",null);
//		Object mailAccountConnectorMailReplacement  = super.getBundleContext().getService(references[0]);		
//		Method method = mailAccountConnectorMailReplacement.getClass().getMethod("getMailDetails");
//		method.setAccessible(true);
//		String message = (String) method.invoke(mailAccountConnectorMailReplacement);
//		
		MailAccountConnectorMailReplacement replacer = mailReplacerAware.waitForService(500);
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