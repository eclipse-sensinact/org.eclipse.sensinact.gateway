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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;
import org.eclipse.sensinact.gateway.app.manager.AppConstant;
import org.eclipse.sensinact.gateway.app.manager.application.AppLifecycleTrigger;
import org.eclipse.sensinact.gateway.app.manager.application.ApplicationService;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.core.Attribute;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;



public class TestLifeCycle {
    private ApplicationStatus status;
    
    private AppServiceMediator mediator=mock(AppServiceMediator.class);
    
    private ApplicationService service=mock(ApplicationService.class);

    @BeforeEach
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        ResourceImpl resource = Mockito.mock(ResourceImpl.class);
        Attribute attribute = Mockito.mock(Attribute.class);
        Mockito.when(attribute.getValue()).thenAnswer(new Answer<ApplicationStatus>() {
            @Override
            public ApplicationStatus answer(InvocationOnMock invocation) throws Throwable {
                return status;
            }
        });
        Mockito.when(resource.getAttribute(DataResource.VALUE)).
                thenReturn(attribute);
        Mockito.when(service.getResource(AppConstant.STATUS)).thenReturn(resource);
    }

    @Test
    public void testNormalCycle() throws Exception {
        AppLifecycleTrigger trigger = new AppLifecycleTrigger(service);
        status = ApplicationStatus.valueOf("INSTALLED");
        ApplicationStatus result = (ApplicationStatus) trigger.execute(new AccessMethodResponseBuilder("AppManager/test/START", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }
        });
        assertTrue(result.equals(ApplicationStatus.valueOf("RESOLVING")));
        status = ApplicationStatus.valueOf("RESOLVING");
        result = (ApplicationStatus) trigger.execute(new AccessMethodResponseBuilder("AppManager/test/START", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }
        });
        assertTrue(result.equals(ApplicationStatus.valueOf("ACTIVE")));
        status = ApplicationStatus.valueOf("ACTIVE");

        result = (ApplicationStatus) trigger.execute(new AccessMethodResponseBuilder("AppManager/test/STOP", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }
        });
        assertTrue(result.equals(ApplicationStatus.valueOf("INSTALLED")));
        status = ApplicationStatus.valueOf("INSTALLED");
        result = (ApplicationStatus) trigger.execute(new AccessMethodResponseBuilder("AppManager/test/UNINSTALL", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }
        });
        assertTrue(result.equals(ApplicationStatus.valueOf("UNINSTALLED")));
    }

    @Test
    public void testUnresolvedCycle() throws Exception {
        AppLifecycleTrigger trigger = new AppLifecycleTrigger(service);
        status = ApplicationStatus.valueOf("INSTALLED");
        ApplicationStatus result = (ApplicationStatus) trigger.execute(new AccessMethodResponseBuilder("AppManager/test/START", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }

            @Override
            public boolean hasError() {
                return true;
            }
        });
        assertTrue(result.equals(ApplicationStatus.valueOf("UNRESOLVED")));
        status = ApplicationStatus.valueOf("UNRESOLVED");
        result = (ApplicationStatus) trigger.execute(new AccessMethodResponseBuilder("AppManager/test/UNINSTALL", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }
        });
        assertTrue(result.equals(ApplicationStatus.valueOf("UNINSTALLED")));
    }

    @Test
    public void testIncompatibleCycle() throws Exception {
        AppLifecycleTrigger trigger = new AppLifecycleTrigger(service);
        AccessMethodResponseBuilder snaMethodResult = new AccessMethodResponseBuilder("AppManager/test/STOP", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }

            @Override
            public boolean hasError() {
                return true;
            }
        };
        status = ApplicationStatus.valueOf("ACTIVE");
        ApplicationStatus result = (ApplicationStatus) trigger.execute(snaMethodResult);
        assertTrue(result.equals(ApplicationStatus.valueOf("INSTALLED")));
        snaMethodResult = new AccessMethodResponseBuilder("AppManager/test/UNINSTALL", null) {
            @Override
            public AccessMethodResponse<?> createAccessMethodResponse(Status status) {
                return null;
            }

            @Override
            public Class<?> getComponentType() {

                return Object.class;
            }
        };
        status = ApplicationStatus.valueOf("ACTIVE");
        result = (ApplicationStatus) trigger.execute(snaMethodResult);
        assertTrue(snaMethodResult.hasError());
        assertTrue(result.equals(ApplicationStatus.valueOf("ACTIVE")));
        status = ApplicationStatus.valueOf("RESOLVING");
        result = (ApplicationStatus) trigger.execute(snaMethodResult);
        assertTrue(snaMethodResult.hasError());
        assertTrue(result.equals(ApplicationStatus.valueOf("RESOLVING")));
    }
}
