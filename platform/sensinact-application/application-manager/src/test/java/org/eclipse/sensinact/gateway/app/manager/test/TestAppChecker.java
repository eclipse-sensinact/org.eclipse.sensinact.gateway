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
package org.eclipse.sensinact.gateway.app.manager.test;

import junit.framework.TestCase;
import org.eclipse.sensinact.gateway.app.api.exception.InvalidApplicationException;
import org.eclipse.sensinact.gateway.app.api.exception.ValidationException;
import org.eclipse.sensinact.gateway.app.manager.checker.ArchitectureChecker;
import org.eclipse.sensinact.gateway.app.manager.checker.JsonValidator;
import org.eclipse.sensinact.gateway.app.manager.json.AppComponent;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
public class TestAppChecker extends TestCase {
    @Mock
    private AppServiceMediator mediator;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        ServiceReference serviceReference = Mockito.mock(ServiceReference.class);
        ServiceReference[] serviceReferences = new ServiceReference[]{serviceReference};
        BundleContext context = Mockito.mock(BundleContext.class);
        Bundle bundle = Mockito.mock(Bundle.class);
        TestInstaller installer = new TestInstaller(mediator, null);
        Mockito.when(mediator.getService(serviceReference)).thenReturn(installer);
        Mockito.when(mediator.getServiceReferences(Mockito.anyString())).thenReturn(serviceReferences);
        Mockito.when(bundle.getResource(Mockito.anyString())).thenAnswer(new Answer<URL>() {
            public URL answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (invocationOnMock.getArguments()[0].equals("/application.json")) {
                    return this.getClass().getResource("/application.json");
                } else if (invocationOnMock.getArguments()[0].equals("/mock_addition.json")) {
                    return this.getClass().getResource("/mock_addition.json");
                } else if (invocationOnMock.getArguments()[0].equals("/mock_set.json")) {
                    return this.getClass().getResource("/mock_set.json");
                }
                return null;
            }
        });
        Mockito.when(context.getBundle()).thenReturn(bundle);
        Mockito.when(mediator.getContext()).thenReturn(context);
    }

    @Test(expected = InvalidApplicationException.class)
    public void testUniqueOutput() throws Exception {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/test_unique_output.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(content);
        String applicationName = new JSONObject(content).getJSONArray("parameters").getJSONObject(0).getString(AppJsonConstant.VALUE);
        JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE);
        List<AppComponent> components = new ArrayList<AppComponent>();
        JSONArray componentArray = json.getJSONArray("application");
        for (int i = 0; i < componentArray.length(); i++) {
            components.add(new AppComponent(mediator, componentArray.getJSONObject(i)));
        }
        ArchitectureChecker.checkApplication(applicationName, components);
    }

    @Test(expected = InvalidApplicationException.class)
    public void testVariableExist() throws Exception {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/test_variable_exist.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(content);
        String applicationName = new JSONObject(content).getJSONArray("parameters").getJSONObject(0).getString(AppJsonConstant.VALUE);
        JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE);
        List<AppComponent> components = new ArrayList<AppComponent>();
        JSONArray componentArray = json.getJSONArray("application");
        for (int i = 0; i < componentArray.length(); i++) {
            components.add(new AppComponent(mediator, componentArray.getJSONObject(i)));
        }
        ArchitectureChecker.checkApplication(applicationName, components);
    }

    @Test(expected = ValidationException.class)
    @Ignore
    public void testInvalidJSONApplication() throws Exception {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/test_invalid_json.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(content);
        JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE);
        JsonValidator.validateApplication(mediator, json);
    }

    @Test
    @Ignore
    public void testValidJSONComponents() throws Exception {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/test_instance.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNotNull(content);
        JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE);
        JsonValidator.validateFunctionsParameters(mediator, json.getJSONArray(AppJsonConstant.APPLICATION));
    }
    /*public void testBoundChecker() {
        assertTrue(true);
    }
    public void testCycleChecker() {
        assertTrue(true);
    }*/
}
