/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.factory.test;

import static org.eclipse.sensinact.gateway.sthbnd.http.factory.HttpMappingProtocolStackEndpointFactory.ENDPOINT_CONFIGURATION_PROP;
import static org.eclipse.sensinact.gateway.sthbnd.http.factory.HttpMappingProtocolStackEndpointFactory.ENDPOINT_TASKS_CONFIGURATION_PROP;
import static org.eclipse.sensinact.gateway.sthbnd.http.factory.HttpMappingProtocolStackEndpointFactory.FACTORY_PID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.osgi.test.common.dictionary.Dictionaries.dictionaryOf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Service;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationPlugin;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ConfigurationExtension.class)
@ExtendWith(ServiceExtension.class)
@TestMethodOrder(OrderAnnotation.class)
public class HttpDeviceFactoryTest {
	
	@InjectService
	Core core; 
	
	@BeforeAll
	public static void setup(@InjectBundleContext BundleContext context) throws Exception {
		context.registerService(Servlet.class, new HttpServlet() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp)
					throws ServletException, IOException {
				String pathInfo = req.getPathInfo();
				
				URL resource = HttpDeviceFactoryTest.class.getResource(pathInfo);
				if(resource == null) {
					resp.sendError(404);
				} else {
					String header = req.getHeader("Accept");
					if(header != null) {
						resp.setContentType(header + "; charset=utf-8");
					}

					PrintWriter pw = resp.getWriter();
					try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream()))) {
						br.lines().forEach(pw::println);
					}
				}
			}
		}, dictionaryOf(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, "/http-factory-test-resources/*"));
		
		context.registerService(ConfigurationPlugin.class, (ref, props) -> {
			String root = System.getProperty("root.folder") + "/";
			
			Enumeration<String> keys = props.keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(key.startsWith("sensinact.http.endpoint"))
					props.put(key, root + props.get(key));
			}
		}, dictionaryOf("cm.target", FACTORY_PID));
	}
	
	@Test
	@Order(1)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
	    location = "?",
		properties = {
				@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test1/config.json"),
				@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test1/tasks.json")
		}
	)
	public void testRawJsonArray() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(2, session.serviceProviders().size());

        testProvider(session, "test1_Foo", "data", "value", "94", "1.2:3.4", 
        		LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
        testProvider(session, "test1_Bar", "data", "value", "28", "5.6:7.8", 
        		LocalDateTime.of(2021, 10, 20, 18, 17).toEpochSecond(ZoneOffset.UTC));
        
        for (ServiceProvider serviceProvider : session.serviceProviders()) {
			serviceProvider.getName();
		}
        
	}

	@Test
	@Order(2)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
		location = "?",
		properties = {
			@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test2/config.json"),
			@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test2/tasks.json")
		}
	)
	public void testOpenDataAPIFormat() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(21, session.serviceProviders().size());
		
		testProvider(session, "test2_1452", "data", "GOOSE_value", "0", "48.849577:2.350867", 
        		LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		testProvider(session, "test2_1452", "data", "DUCK_value", "0", "48.849577:2.350867", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));

		testProvider(session, "test2_1151", "data", "GOOSE_value", "122", "48.882697:2.344013", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		testProvider(session, "test2_1151", "data", "DUCK_value", "7", "48.882697:2.344013", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		testProvider(session, "test2_1151", "data", "SWAN_value", "4", "48.882697:2.344013", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		testProvider(session, "test2_1151", "data", "PEACOCK_value", "0", "48.882697:2.344013", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		
	}
	
	@Test
	@Order(3)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
	    location = "?",
		properties = {
				@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test3/config.json"),
				@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test3/tasks.json")
		}
	)
	public void testJsonArrayNestedInArray() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(2, session.serviceProviders().size());

        testProvider(session, "test3_Foo", "data", "value", "94", "1.2:3.4", 
        		LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
        testProvider(session, "test3_Bar", "data", "value", "28", "5.6:7.8", 
        		LocalDateTime.of(2021, 10, 20, 18, 17).toEpochSecond(ZoneOffset.UTC));
        
        for (ServiceProvider serviceProvider : session.serviceProviders()) {
			serviceProvider.getName();
		}
        
	}

	@Test
	@Order(4)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
	location = "?",
	properties = {
			@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test4/config.json"),
			@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test4/tasks.json")
	}
			)
	public void testCsvWithTitles() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(2, session.serviceProviders().size());
		
		testProvider(session, "test4_Foo", "data", "value", "94", "1.2:3.4", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		testProvider(session, "test4_Bar", "data", "value", "28", "5.6:7.8", 
				LocalDateTime.of(2021, 10, 20, 18, 17).toEpochSecond(ZoneOffset.UTC));
		
		for (ServiceProvider serviceProvider : session.serviceProviders()) {
			serviceProvider.getName();
		}
		
	}
	
	@Test
	@Order(5)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
	location = "?",
	properties = {
			@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test5/config.json"),
			@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test5/tasks.json")
	}
			)
	public void testCsvWithoutTitles() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(2, session.serviceProviders().size());
		
		testProvider(session, "test5_Foo", "data", "value", "94", "1.2:3.4", 
				LocalDateTime.of(2021, 10, 20, 18, 14).toEpochSecond(ZoneOffset.UTC));
		testProvider(session, "test5_Bar", "data", "value", "28", "5.6:7.8", 
				LocalDateTime.of(2021, 10, 20, 18, 17).toEpochSecond(ZoneOffset.UTC));
		
		for (ServiceProvider serviceProvider : session.serviceProviders()) {
			serviceProvider.getName();
		}
		
	}

	@Test
	@Order(6)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
	location = "?",
	properties = {
			@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test6/config.json"),
			@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test6/tasks.json")
	}
			)
	public void testCsvWithNumberLikeValues() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(5, session.serviceProviders().size());
		
		testProvider(session, "test6_1114", "vehicle", "line", "3", "59.445000:24.742240", -1);
		testProvider(session, "test6_1119", "vehicle", "line", "43", "59.429320:24.698510", -1);
		testProvider(session, "test6_1120", "vehicle", "line", "20A", "59.404720:24.681790", -1);
		testProvider(session, "test6_1121", "vehicle", "line", "59", "59.441820:24.742210", -1);
		testProvider(session, "test6_1141", "vehicle", "line", "24A", "59.404510:24.654770", -1);

		for (ServiceProvider serviceProvider : session.serviceProviders()) {
			serviceProvider.getName();
		}
		
	}

	@Test
	@Order(7)
	@WithFactoryConfiguration(factoryPid = FACTORY_PID, name = "test",
	location = "?",
	properties = {
			@Property(key = ENDPOINT_CONFIGURATION_PROP, value="src/test/resources/test7/config.json"),
			@Property(key = ENDPOINT_TASKS_CONFIGURATION_PROP, value="src/test/resources/test7/tasks.json")
	}
			)
	public void testCsvWithForeignLocale() throws Exception {
		Thread.sleep(5000);
		
		Session session = core.getAnonymousSession();
		
		assertEquals(1, session.serviceProviders().size());
		
		testProvider(session, "test7_Test", "pollutant", "O3", "42.2", "1.23:4.56", 
				LocalDateTime.of(2021, 11, 05, 18, 00).toEpochSecond(ZoneOffset.UTC));
		
		
		for (ServiceProvider serviceProvider : session.serviceProviders()) {
			serviceProvider.getName();
		}
		
	}

	private void testProvider(Session session, String providerName, String serviceName, String resourceName,
			String value, String location, long timestamp) {
		ServiceProvider provider = session.serviceProvider(providerName);
        assertNotNull(provider);
        
        Service service = provider.getService(serviceName);
        Resource variable = service.getResource(resourceName);
        
        SnaMessage<?> response = variable.get(DataResource.VALUE, (Object[]) null);
        JSONObject jsonObject = new JSONObject(response.getJSON());
        
        assertEquals(value, String.valueOf(jsonObject.getJSONObject("response").get("value")));

        service = provider.getService("admin");
        variable = service.getResource("location");
        
        response = variable.get(DataResource.VALUE, (Object[]) null);
        jsonObject = new JSONObject(response.getJSON());
        
        assertEquals(location, jsonObject.getJSONObject("response").get("value"));
        
        //TODO how to test the timestamp?
	}
}
