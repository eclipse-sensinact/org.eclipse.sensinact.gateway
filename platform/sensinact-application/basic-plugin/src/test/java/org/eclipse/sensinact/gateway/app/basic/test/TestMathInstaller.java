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

import junit.framework.TestCase;
import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.basic.installer.BasicInstaller;
import org.eclipse.sensinact.gateway.app.basic.math.AdditionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.DivisionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.ModuloFunction;
import org.eclipse.sensinact.gateway.app.basic.math.MultiplicationFunction;
import org.eclipse.sensinact.gateway.app.basic.math.SubtractionFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppJsonConstant;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.charset.Charset;

@RunWith(PowerMockRunner.class)
public class TestMathInstaller extends TestCase {
    @Mock
    private AppServiceMediator mediator;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testAdditionCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_addition.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller(mediator);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction function = installer.getFunction(appFunction);
            assertTrue(function instanceof AdditionFunction);
        }
    }

    public void testSubtractionCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_subtraction.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller(mediator);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction function = installer.getFunction(appFunction);
            assertTrue(function instanceof SubtractionFunction);
        }
    }

    public void testMultiplicationCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_multiplication.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller(mediator);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction function = installer.getFunction(appFunction);
            assertTrue(function instanceof MultiplicationFunction);
        }
    }

    public void testDivisionCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_division.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller(mediator);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction function = installer.getFunction(appFunction);
            assertTrue(function instanceof DivisionFunction);
        }
    }

    public void testModuloCreation() {
        String content = null;
        try {
            content = TestUtils.readFile(this.getClass().getResourceAsStream("/simple_modulo.json"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content != null) {
            JSONObject json = new JSONObject(content).getJSONArray("parameters").getJSONObject(1).getJSONObject(AppJsonConstant.VALUE).getJSONArray("application").getJSONObject(0);
            BasicInstaller installer = new BasicInstaller(mediator);
            AppFunction appFunction = new AppFunction(json.getJSONObject(AppJsonConstant.APP_FUNCTION));
            AbstractFunction function = installer.getFunction(appFunction);
            assertTrue(function instanceof ModuloFunction);
        }
    }
}
