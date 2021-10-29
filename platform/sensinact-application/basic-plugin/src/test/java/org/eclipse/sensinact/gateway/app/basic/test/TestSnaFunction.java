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
package org.eclipse.sensinact.gateway.app.basic.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginHook;
import org.eclipse.sensinact.gateway.app.basic.sna.ActActionFunction;
import org.eclipse.sensinact.gateway.app.basic.sna.SetActionFunction;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.eclipse.sensinact.gateway.app.manager.component.data.ResourceData;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.ModelConfiguration;
import org.eclipse.sensinact.gateway.core.ModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.core.ModelInstance;
import org.eclipse.sensinact.gateway.core.ModelInstanceBuilder;
import org.eclipse.sensinact.gateway.core.PropertyResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.ResourceBuilder;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.SensiNactResourceModel;
import org.eclipse.sensinact.gateway.core.ServiceBuilder;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.ServiceProviderImpl;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.TypeConfig;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.ActResponse;
import org.eclipse.sensinact.gateway.core.method.DescribeMethod.DescribeType;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.eclipse.sensinact.gateway.core.method.SetResponse;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.AccessTreeImpl;
import org.eclipse.sensinact.gateway.core.security.AuthorizationService;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSnaFunction{
	
	private static final Logger LOG = LoggerFactory.getLogger(TestSnaFunction.class);

    private String lightState;
    private String displayText;
    private Integer displayTimeout;
    
    private AppServiceMediator mediator=Mockito.mock(AppServiceMediator.class);
    private ModelInstance modelInstance;

    private Session session=Mockito.mock(Session.class);
    private PropertyResource dimResource;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void init() throws Exception {
        AuthorizationService authorization = Mockito.mock(AuthorizationService.class);
        SecuredAccess securedAccess = Mockito.mock(SecuredAccess.class);
        Mockito.when(mediator.callService(Mockito.any(Class.class), Mockito.any(Executable.class))).thenReturn(new BundleValidation() {
            @Override
            public String check(Bundle bundle) throws BundleValidationException {
                return "xxxxxxxxxxx00000000";
            }
        });
        Mockito.when(authorization.getAuthenticatedAccessLevelOption(Mockito.anyString(), Mockito.anyLong())).thenReturn(AccessLevelOption.ANONYMOUS);

        Mockito.when(authorization.getAuthenticatedAccessLevelOption(Mockito.anyString(), Mockito.anyString())).thenReturn(AccessLevelOption.ANONYMOUS);

        Mockito.when(securedAccess.getAccessTree(Mockito.any(String.class))).thenReturn(new AccessTreeImpl().withAccessProfile(AccessProfileOption.ALL_ANONYMOUS));
        BundleContext context = Mockito.mock(BundleContext.class);

        final ServiceReference reference = Mockito.mock(ServiceReference.class);
        final ServiceReference referenceAuth = Mockito.mock(ServiceReference.class);

        Mockito.when(context.getServiceReferences(Mockito.any(Class.class), Mockito.anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Collection<ServiceReference> answer(InvocationOnMock invocation) throws Throwable {
                if (SecuredAccess.class.isAssignableFrom((Class) invocation.getArguments()[0])) {
                    return Collections.singletonList(reference);
                } else if (AuthorizationService.class.isAssignableFrom((Class) invocation.getArguments()[0])) {
                    return Collections.singletonList(referenceAuth);
                }
                return null;
            }
        });
        Mockito.when(context.getServiceReferences(Mockito.anyString(), Mockito.anyString())).thenAnswer(new Answer<ServiceReference[]>() {
            @Override
            public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
                if (SecuredAccess.class.getCanonicalName().equals(invocation.getArguments()[0])) {
                    return new ServiceReference[]{reference};
                } else if (AuthorizationService.class.getCanonicalName().equals(invocation.getArguments()[0])) {
                    return new ServiceReference[]{referenceAuth};
                }
                return null;
            }
        });
        Mockito.when(context.getServiceReference(Mockito.eq(SecuredAccess.class))).thenReturn(reference);

        Mockito.when(context.getServiceReference(Mockito.eq(AuthorizationService.class))).thenReturn(referenceAuth);

        Mockito.when(context.getService(reference)).thenReturn(securedAccess);
        Mockito.when(context.getService(referenceAuth)).thenReturn(authorization);

        ServiceRegistration registration = Mockito.mock(ServiceRegistration.class);

        Mockito.when(context.registerService(Mockito.eq(SensiNactResourceModel.class), Mockito.any(SensiNactResourceModel.class), Mockito.any(Dictionary.class))).thenReturn(registration);
        Mockito.when(context.registerService(Mockito.eq(SensiNactResourceModel.class.getCanonicalName()), Mockito.any(SensiNactResourceModel.class), Mockito.any(Dictionary.class))).thenReturn(registration);

        BundleWiring wiring = Mockito.mock(BundleWiring.class);

        Mockito.when(wiring.getClassLoader()).thenReturn(Thread.currentThread().getContextClassLoader());

        Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.when(bundle.adapt(Mockito.eq(BundleWiring.class))).thenReturn(wiring);
        Mockito.when(bundle.getState()).thenReturn(Bundle.ACTIVE);
        Mockito.when(bundle.getSymbolicName()).thenReturn("fakeBundle");

        Mockito.when(context.getBundle()).thenReturn(bundle);

        this.mediator = new AppServiceMediator(context);

        this.modelInstance = new ModelInstanceBuilder(mediator
        	).build("SimulatedLight_001", null, new ModelConfigurationBuilder(
				mediator, ModelConfiguration.class,ModelInstance.class
				).withStartAtInitializationTime(true).build());
		
        ServiceProviderImpl serviceProvider = modelInstance.getRootElement();
        ServiceImpl service = serviceProvider.addService("LightService_SimulatedLight_001");
        ResourceImpl resource = service.addDataResource(PropertyResource.class, "DIM", int.class, 0);
        dimResource = resource.getProxy(new AccessTreeImpl<>().withAccessProfile(AccessProfileOption.ALL_ANONYMOUS));
        
        Mockito.when(session.resource(Mockito.eq("SimulatedLight_001"), Mockito.eq("LightService_SimulatedLight_001"), Mockito.eq("TURN_ON"))).thenReturn(new StateActionResource(mediator, "TURN_ON", this));

        Mockito.when(session.resource(Mockito.eq("SimulatedTV_001"), Mockito.eq("DisplayService_SimulatedTV_001"), Mockito.eq("DISPLAY"))).thenReturn(new DisplayActionResource(mediator, this));

        Mockito.when(session.resource(Mockito.eq("SimulatedLight_001"), Mockito.eq("LightService_SimulatedLight_001"), Mockito.eq("DIM"))).thenReturn(dimResource);
   
        Mockito.when(session.getResource(Mockito.eq("SimulatedLight_001"), Mockito.eq("LightService_SimulatedLight_001"), Mockito.eq("TURN_ON"))).thenReturn(
    		new DescribeResponse<JSONObject>("/SimulatedLight_001/LightService_SimulatedLight_001/TURN_ON", Status.SUCCESS, DescribeType.RESOURCE) {
    			{this.putValue(SnaConstants.RESPONSE_KEY, new JSONObject().put(Resource.TYPE, Resource.Type.ACTION.name()));}
    		});
        Mockito.when(session.getResource(Mockito.eq("SimulatedTV_001"), Mockito.eq("DisplayService_SimulatedTV_001"), Mockito.eq("DISPLAY"))).thenReturn(
        	new DescribeResponse<JSONObject>("/SimulatedTV_001/DisplayService_SimulatedTV_001/DISPLAY", Status.SUCCESS, DescribeType.RESOURCE) {
    			{this.putValue(SnaConstants.RESPONSE_KEY, new JSONObject().put(Resource.TYPE, Resource.Type.ACTION.name()));}
        	});
        Mockito.when(session.getResource(Mockito.eq("SimulatedLight_001"), Mockito.eq("LightService_SimulatedLight_001"), Mockito.eq("DIM"))).thenReturn(
        	new DescribeResponse<JSONObject>("/SimulatedLight_001/LightService_SimulatedLight_001/DIM", Status.SUCCESS, DescribeType.RESOURCE) {
    			{this.putValue(SnaConstants.RESPONSE_KEY, new JSONObject().put(Resource.TYPE, Resource.Type.PROPERTY.name()));}
        	});
        final ActResponse actionResponse = Mockito.mock(ActResponse.class);
        Mockito.when(session.act(Mockito.eq("SimulatedLight_001"), Mockito.eq("LightService_SimulatedLight_001"), Mockito.eq("TURN_ON"), Mockito.any(Object[].class))).thenAnswer(
        	new Answer<ActResponse>() {
			@Override
			public ActResponse answer(InvocationOnMock invocation) throws Throwable {
				TestSnaFunction.this.setState("TURN_ON");
				return actionResponse;
			}        	
        });

        Mockito.when(session.act(Mockito.eq("SimulatedTV_001"), Mockito.eq("DisplayService_SimulatedTV_001"), Mockito.eq("DISPLAY"), Mockito.any(Object[].class))).thenAnswer(
        	new Answer<ActResponse>() {
			@Override
			public ActResponse answer(InvocationOnMock invocation) throws Throwable {
				Object[] objects = (Object[]) invocation.getArguments()[3];
				TestSnaFunction.this.setDisplay((String) objects[0], (Integer)objects[1]);
				return actionResponse;
			}        	
        });

        final SetResponse setResponse = Mockito.mock(SetResponse.class);
        Mockito.when(session.set(Mockito.eq("SimulatedLight_001"), Mockito.eq("LightService_SimulatedLight_001"), Mockito.eq("DIM"),Mockito.anyString(), Mockito.any(Object[].class))).thenAnswer(
        	new Answer<SetResponse>() {
			@Override
			public SetResponse answer(InvocationOnMock invocation) throws Throwable {
				Object[] objects = invocation.getArguments();
				TestSnaFunction.this.dimResource.set(objects[4]);
				return setResponse;
			}        	
        });
    }

    public void testActActionNoParameters() {
        this.lightState = null;
        ActActionFunction function = new ActActionFunction();
        function.setListener(new FunctionUpdateListener() {
            @Override
            public void updatedResult(Object result) {
                assertTrue(result instanceof PluginHook);
            }
        });
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ResourceData(session, "/SimulatedLight_001/LightService_SimulatedLight_001/TURN_ON"));
        function.process(variables);
        function.fireHook();
        assertTrue(this.lightState.equals("TURN_ON"));
    }

    public void testActActionWithParameters() {
        this.displayText = null;
        this.displayTimeout = null;
        ActActionFunction function = new ActActionFunction();
        function.setListener(new FunctionUpdateListener() {
            @Override
            public void updatedResult(Object result) {
                assertTrue(result instanceof PluginHook);
            }
        });
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ResourceData(session, "/SimulatedTV_001/DisplayService_SimulatedTV_001/DISPLAY"));
        variables.add(new ConstantData("Welcome", String.class));
        variables.add(new ConstantData(10, Integer.class));
        function.process(variables);
        function.fireHook();
        assertTrue(this.displayText.equals("Welcome"));
        assertTrue(this.displayTimeout.equals(10));
    }

    public void testSetAction() {
        SetActionFunction function = new SetActionFunction();
        function.setListener(new FunctionUpdateListener() {
            @Override
            public void updatedResult(Object result) {
                assertTrue(result instanceof PluginHook);
            }
        });
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ResourceData(session, "/SimulatedLight_001/LightService_SimulatedLight_001/DIM"));
        variables.add(new ConstantData(10, Integer.class));
        function.process(variables);
        function.fireHook();
        assertTrue(dimResource.get().getResponse(DataResource.VALUE).equals(10));
    }

    public void setState(String action) {
        this.lightState = action;
    }

    public void setDisplay(String text, Integer timeout) {
        this.displayText = text;
        this.displayTimeout = timeout;
    }

    /**
     * Returns a new {@link ServiceBuilder} instance
     *
     * @return a new {@link ServiceBuilder} instance
     */
    public ServiceBuilder createServiceBuilder() {
        ServiceBuilder builder = new ServiceBuilder(this.mediator, ServiceImpl.class);

        builder.configureImplementationClass(ServiceImpl.class);

        return builder;
    }

    /**
     * Returns a new {@link ResourceBuilder} instance to create
     * a new {@link ResourceImpl} whose proxied type is the one
     * passed as parameter
     *
     * @param implementationInterface the extended {@link Resource} type that will be
     *                                used to create new {@link ResourceImpl} instance(s)
     * @return a new {@link ResourceBuilder} instance
     */
    public ResourceBuilder createResourceBuilder(Class<? extends Resource> implementationInterface) {
        ResourceConfig resourceConfig = new ResourceConfig();
        Class<? extends Resource> resourceType = implementationInterface;

        if (resourceType == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to create a resource builder : null resource type");
            }
            return null;
        }
        TypeConfig typeConfig = new TypeConfig(resourceType);
        resourceConfig.setTypeConfig(typeConfig);

        ResourceBuilder builder = new ResourceBuilder(this.mediator, resourceConfig);

        return builder;
    }
}