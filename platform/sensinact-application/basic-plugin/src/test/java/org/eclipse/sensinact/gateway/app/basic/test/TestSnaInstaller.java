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
import org.eclipse.sensinact.gateway.app.basic.sna.ActActionFunction;
import org.eclipse.sensinact.gateway.app.basic.sna.SetActionFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

public class TestSnaInstaller {

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


    public void testActActionCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_act_no_parameters.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller();
            installer.activate(context);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction<?> function = installer.getFunction(appFunction);
            assertTrue(function instanceof ActActionFunction);
        }
    }

    public void testSetActionCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_set.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller();
            installer.activate(context);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction<?> function = installer.getFunction(appFunction);
            assertTrue(function instanceof SetActionFunction);
        }
    }
}
