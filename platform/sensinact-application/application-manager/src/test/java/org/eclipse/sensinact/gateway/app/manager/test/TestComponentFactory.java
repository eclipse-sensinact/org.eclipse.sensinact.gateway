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

import org.eclipse.sensinact.gateway.api.core.ActionResource;
import org.eclipse.sensinact.gateway.api.core.DataResource;
import org.eclipse.sensinact.gateway.api.core.Resource;
import org.eclipse.sensinact.gateway.api.core.ServiceProvider;
import org.eclipse.sensinact.gateway.app.api.exception.ApplicationFactoryException;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.component.Component;
import org.eclipse.sensinact.gateway.app.manager.component.property.RegisterPropertyBlock;
import org.eclipse.sensinact.gateway.app.manager.factory.ApplicationFactory;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceDescriptor;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.core.method.legacy.GetResponse;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.ServiceReference;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Map;

@RunWith(PowerMockRunner.class)
public class TestComponentFactory extends TestCase {
    @Mock
    private AppModelInstance modelInstance;

    @Mock
    private ModelConfiguration modelConfiguration;

    @Mock
    private ServiceProviderImpl device;
    @Mock
    private AppServiceMediator mediator;
    @Mock
    private Resource resource;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        GetResponse getResponse = Mockito.mock(GetResponse.class);
        ServiceReference serviceReference = Mockito.mock(ServiceReference.class);
        ServiceReference[] serviceReferences = new ServiceReference[]{serviceReference};

        Session session = Mockito.mock(Session.class);
        PluginInstaller installer = Mockito.mock(PluginInstaller.class);
        ServiceImpl adminService = Mockito.mock(ServiceImpl.class);
        Mockito.when(adminService.getName()).thenReturn(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
        Mockito.when(adminService.getPath()).thenReturn(UriUtils.getUri(new String[]{"TestAppDevice", ServiceProvider.ADMINISTRATION_SERVICE_NAME}));
        Mockito.when(mediator.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
        Mockito.when(modelInstance.mediator()).thenReturn(mediator);

        Mockito.when(adminService.getModelInstance()).thenReturn(modelInstance);
        Mockito.when(device.getModelInstance()).thenReturn(modelInstance);

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.setTypeConfig(new TypeConfig(ActionResource.class));
        ResourceBuilder builder = new ResourceBuilder(mediator, resourceConfig);
        builder.configureName(AppConstant.UNINSTALL);

        Mockito.when(device.getAdminService()).thenReturn(adminService);
        Mockito.when(device.getName()).thenReturn("TestAppDevice");
        Mockito.when(device.getPath()).thenReturn(UriUtils.getUri(new String[]{"TestAppDevice"}));

        ResourceImpl uninstallResource = builder.build(modelInstance, adminService);
        Mockito.when(adminService.getResource(AppConstant.UNINSTALL)).thenReturn(uninstallResource);
        Mockito.when(getResponse.getResponse(DataResource.TYPE)).thenReturn("int");
        Mockito.when(resource.get(DataResource.VALUE)).thenReturn(getResponse);
        Mockito.when(session.resource(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(resource);
        Mockito.when(device.getName()).thenReturn("TestAppDevice");
        Mockito.when(installer.getFunction(Mockito.any(AppFunction.class))).thenReturn(new MockComponentIncrement(mediator));
        Mockito.when(mediator.getService(serviceReference)).thenReturn(installer);
        Mockito.when(mediator.getServiceReferences(Mockito.anyString())).thenReturn(serviceReferences);
        Mockito.when(modelConfiguration.getResourceDescriptor()).thenReturn(new ResourceDescriptor());

        Mockito.when(modelInstance.configuration()).thenReturn(modelConfiguration);
        Mockito.when(modelInstance.getResourceBuilder(Mockito.any(ResourceDescriptor.class), Mockito.anyByte())).then(new Answer<ResourceBuilder>() {
            @Override
            public ResourceBuilder answer(InvocationOnMock invocation) throws Throwable {
                return TestUtils.createResourceBuilder(mediator, (ResourceDescriptor) invocation.getArguments()[0]);
            }
        });
        Mockito.when(modelInstance.createResourceBuilder(Mockito.any(ResourceDescriptor.class))).then(new Answer<ResourceBuilder>() {
            @Override
            public ResourceBuilder answer(InvocationOnMock invocation) throws Throwable {
                return TestUtils.createResourceBuilder(mediator, (ResourceDescriptor) invocation.getArguments()[0]);
            }
        });
    }

    @Test
    public void testComponentCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/test_factory.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            String name = new JSONObject(content).getJSONArray("parameters").getJSONObject(0).getString(AppJsonConstant.VALUE);
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE);
            AppContainer container = new AppContainer(mediator, name, json);
            assertNotNull(container);
            ApplicationService service = null;
            try {
                service = new ApplicationService(modelInstance, "Test", device);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assertNotNull(service);
            Application application = null;
            try {
                application = ApplicationFactory.createApplication(mediator, container, service);
            } catch (ApplicationFactoryException e) {
                e.printStackTrace();
            }
            assertNotNull(application);
            try {
                service.createSnaService(container, application);
            } catch (InvalidResourceException e) {
                e.printStackTrace();
            } catch (InvalidValueException e) {
                e.printStackTrace();
            }
            try {
                Field componentsField = Application.class.getDeclaredField("components");
                componentsField.setAccessible(true);
                Map<String, Component> components = (Map<String, Component>) componentsField.get(application);
                assertTrue(components.size() == 1);
                assertNotNull(components.get("/simple_test/component1"));
                assertTrue(components.get("/simple_test/component1").getProperty("register") instanceof RegisterPropertyBlock);
                assertNotNull(container.getResourceUris());
                assertTrue(container.getResourceUris().size() == 2);
                assertTrue(container.getInitialize().getOptions().getAutoStart());
                assertTrue(container.getInitialize().getOptions().getResetOnStop());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
