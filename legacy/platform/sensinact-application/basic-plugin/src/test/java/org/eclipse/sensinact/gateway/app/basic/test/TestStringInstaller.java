/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.basic.installer.BasicInstaller;
import org.eclipse.sensinact.gateway.app.basic.string.ConcatenateFunction;
import org.eclipse.sensinact.gateway.app.basic.string.SubstringFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import jakarta.json.JsonObject;

public class TestStringInstaller   {

    private ComponentContext context;
    private BundleContext bundleContext;
    private Bundle bundle;

    @BeforeEach
    public void init() throws Exception {
        context = Mockito.mock(ComponentContext.class);
        bundle = Mockito.mock(Bundle.class);
        bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(bundleContext.getBundle()).thenReturn(bundle);
        Mockito.when(context.getBundleContext()).thenReturn(bundleContext);
    }


    public void testConcatenateCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_concatenate.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JsonObject json = JsonProviderFactory.readObject(content).getJsonArray("parameters").getJsonObject(1).getJsonObject(AppJsonConstant.VALUE).getJsonArray("application").getJsonObject(0);
            BasicInstaller installer = new BasicInstaller();
            installer.activate(context);
            AppFunction appFunction = new AppFunction(json.getJsonObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction<?> function = installer.getFunction(appFunction);
            assertTrue(function instanceof ConcatenateFunction);
        }
    }

    public void testSubtractionCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_substring.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JsonObject json = JsonProviderFactory.readObject(content).getJsonArray("parameters").getJsonObject(1).getJsonObject(AppJsonConstant.VALUE).getJsonArray("application").getJsonObject(0);
            BasicInstaller installer = new BasicInstaller();
            installer.activate(context);
            AppFunction appFunction = new AppFunction(json.getJsonObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction<?> function = installer.getFunction(appFunction);
            assertTrue(function instanceof SubstringFunction);
        }
    }
}
