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
package org.eclipse.sensinact.gateway.app.manager.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.app.api.exception.ApplicationFactoryException;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginHook;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.Application;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.component.DataProvider;
import org.eclipse.sensinact.gateway.app.manager.component.DataProviderItf;
import org.eclipse.sensinact.gateway.app.manager.factory.ApplicationFactory;
import org.eclipse.sensinact.gateway.app.manager.json.AppContainer;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ActionResource;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.InvalidResourceException;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceDescriptor;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.GetResponse;
import org.eclipse.sensinact.gateway.core.method.SubscribeResponse;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class TestSubscription   implements TestResult {
    
    private AppModelInstance modelInstance=mock(AppModelInstance.class);

    
    private ModelConfiguration modelConfiguration=mock(ModelConfiguration.class);
    
    private ServiceProviderImpl device=mock(ServiceProviderImpl.class);

    
    private ServiceImpl service=mock(ServiceImpl.class);
    
    private AppServiceMediator mediator=mock(AppServiceMediator.class);
    
    private Resource resource=mock(Resource.class);
    
    private Core core=mock(Core.class);

    private SnaMessage message;
    private int result;
    private Map<String, DataProviderItf> mockedRegistry;
    /**
     * @throws Exception
     */
    /**
     * @throws Exception
     */
    @BeforeEach
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockedRegistry = new HashMap<String, DataProviderItf>();
        ServiceReference serviceReferenceInstaller = Mockito.mock(ServiceReference.class);
        ServiceReference serviceReferenceResource = Mockito.mock(ServiceReference.class);
        final ServiceRegistration serviceRegistration = Mockito.mock(ServiceRegistration.class);
        @SuppressWarnings("rawtypes") ServiceReference[] serviceReferencesInstaller = new ServiceReference[]{serviceReferenceInstaller};
        @SuppressWarnings("rawtypes") ServiceReference[] serviceReferencesActionHook = new ServiceReference[]{};
        @SuppressWarnings("rawtypes") ServiceReference[] serviceReferencesResource = new ServiceReference[]{serviceReferenceResource};
        // Mock of the session
        final Session session = Mockito.mock(Session.class);

        // Mock of the responses
        GetResponse getResponse = Mockito.mock(GetResponse.class);
        SubscribeResponse subscribeResponse = Mockito.mock(SubscribeResponse.class);
        // Mock of the sensiNact AppManager admin service
        Mockito.when(service.getName()).thenReturn(ServiceProvider.ADMINISTRATION_SERVICE_NAME);
        Mockito.when(service.getPath()).thenReturn(UriUtils.getUri(new String[]{"TestAppDevice", ServiceProvider.ADMINISTRATION_SERVICE_NAME}));
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
        Mockito.when(service.getModelInstance()).thenReturn(modelInstance);
        Mockito.when(device.getModelInstance()).thenReturn(modelInstance);

        Mockito.when(mediator.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());
        Mockito.when(modelInstance.mediator()).thenReturn(mediator);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.setTypeConfig(new TypeConfig(ActionResource.class));
        ResourceBuilder builder = new ResourceBuilder(mediator, resourceConfig);
        builder.configureName(AppConstant.UNINSTALL);
        ResourceImpl uninstallResource = builder.build(modelInstance, service);
        // Mock of the OSGi objects
        ServiceRegistration registration = Mockito.mock(ServiceRegistration.class);
        BundleContext context = Mockito.mock(BundleContext.class);
        Mockito.when(context.registerService(Mockito.anyString(), Mockito.any(), Mockito.any(Dictionary.class))).thenReturn(registration);

        Mockito.when(mediator.getContext()).thenReturn(context);
        Mockito.when(getResponse.getResponse(DataResource.VALUE)).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new JSONObject(message.getJSON()).getJSONObject("notification").getInt(DataResource.VALUE);
            }
        });
        Mockito.when(getResponse.getStatusCode()).thenReturn(200);
        Mockito.when(getResponse.getResponse(DataResource.TYPE)).thenReturn("int");
        Mockito.when(resource.get(DataResource.TYPE)).thenReturn(getResponse);
        Mockito.when(resource.get(DataResource.VALUE)).thenReturn(getResponse);
        Mockito.when(session.resource(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(resource);

        Mockito.when(session.get(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(getResponse);
        
        Mockito.when(session.getResource(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(
    		new DescribeResponse<JSONObject>(mediator, "/SimulatedSlider_01/SliderService_SimulatedSlider_01/slider", Status.SUCCESS, DescribeType.RESOURCE) {});

        Mockito.when(session.subscribe(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Recipient.class), Mockito.any(JSONArray.class))).thenReturn(
    		subscribeResponse);
        
        Mockito.when(session.resource(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(resource);
        
        Mockito.when(subscribeResponse.getResponse(String.class, SnaConstants.SUBSCRIBE_ID_KEY)).thenReturn("id");
        Mockito.when(resource.subscribe(Mockito.anyString(), Mockito.any(Recipient.class), Mockito.anySet())).thenReturn(subscribeResponse);
        Mockito.when(device.getName()).thenReturn("TestAppDevice");
        Mockito.when(device.getPath()).thenReturn(UriUtils.getUri(new String[]{"TestAppDevice"}));
        PluginInstaller installer = new TestInstaller(mediator, this);
        Mockito.when(mediator.getService(serviceReferenceInstaller)).thenReturn(installer);
        Mockito.when(mediator.getServiceReferences("(objectClass=" + PluginInstaller.class.getCanonicalName() + ")")).thenReturn(serviceReferencesInstaller);
        Mockito.when(service.getResource(AppConstant.UNINSTALL)).thenReturn(uninstallResource);
        Mockito.when(device.getAdminService()).thenReturn(service);
        // Mocking the DataProviderItf for the ActionHookQueue, i.e., an empty set of ServiceReference
        Mockito.when(mediator.getServiceReferences("(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(type=" + PluginHook.class.getName() + "))")).thenReturn(serviceReferencesActionHook);
        Mockito.when(mediator.getService(serviceReferenceResource)).thenAnswer(new Answer<DataProviderItf>() {
            @Override
            public DataProviderItf answer(InvocationOnMock invocationOnMock) throws Throwable {
                DataProviderItf d = mockedRegistry.get("/SimulatedSlider_01/SliderService_SimulatedSlider_01/slider");
                return d;
            }
        });
        Mockito.when(mediator.getServiceReferences("(&(objectClass=" + DataProviderItf.class.getName() + ")" + "(uri=/SimulatedSlider_01/SliderService_SimulatedSlider_01/slider))")).thenReturn(serviceReferencesResource);
        Mockito.when(mediator.registerService(Mockito.eq(DataProviderItf.class.getCanonicalName()), Mockito.any(DataProvider.class), Mockito.any(Dictionary.class))).thenAnswer(new Answer<ServiceRegistration>() {
            @Override
            public ServiceRegistration answer(InvocationOnMock invocationOnMock) throws Throwable {
                mockedRegistry.put(((Dictionary<String, String>) invocationOnMock.getArguments()[2]).get("uri"), (DataProviderItf) invocationOnMock.getArguments()[1]);
                return serviceRegistration;
            }
        });
        Mockito.when(core.getApplicationSession(Mockito.any(Mediator.class), Mockito.anyString())).thenAnswer(new Answer<Session>() {
            @Override
            public Session answer(InvocationOnMock invocation) throws Throwable {
                return session;
            }

        });

        Mockito.when(mediator.callService(Mockito.eq(Core.class), Mockito.any(Executable.class))).then(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object result = ((Executable) invocation.getArguments()[1]).execute(core);
                return result;
            }
        });
        this.result = 0;
    }

    @Test
    public void testSubscription() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/test_subscription.json"), Charset.defaultCharset());

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            String name = new JSONObject(content).getJSONArray("parameters").getJSONObject(0).getString(AppJsonConstant.VALUE);
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE);
            AppContainer container = new AppContainer(mediator, name, json);
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
            application.start();
            message = new AppTestSnaMessage(mediator, "/SimulatedSlider_01/SliderService_SimulatedSlider_01/slider", int.class, 100);
            try {
                application.callback("id", new SnaMessage[]{message});
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(result == 0);
            message = new AppTestSnaMessage(mediator, "/SimulatedSlider_01/SliderService_SimulatedSlider_01/slider", int.class, 600);
            try {
                application.callback("id", new SnaMessage[]{message});
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(result == 600);
            message = new AppTestSnaMessage(mediator, "/SimulatedSlider_01/SliderService_SimulatedSlider_01/slider", int.class, 200);
            try {
                application.callback("id", new SnaMessage[]{message});
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(result == 600);
        }
    }

    public void setValue(int value) {
        result = value;
    }
}
