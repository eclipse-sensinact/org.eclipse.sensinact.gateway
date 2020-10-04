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

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.plugin.AbstractPlugin;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.json.JSONObject;

class TestInstaller extends AbstractPlugin {
    private TestResult test;
    private AppServiceMediator mediator;

    TestInstaller(AppServiceMediator mediator, TestResult test) {
        this.mediator = mediator;
        this.test = test;
    }

    public JSONObject getComponentJSONSchema(String function) {
        if (function.equals("mock_addition")) {
            return MockComponentAddition.getJSONSchemaFunction(mediator.getContext());
        } else if (function.equals("mock_set")) {
            return MockComponentSetter.getJSONSchemaFunction(mediator.getContext());
        }
        return null;
    }

    public AbstractFunction getFunction(AppFunction function) {
        if (function.getName().equals("mock_addition")) {
            return new MockComponentAddition(mediator);
        } else if (function.getName().equals("mock_set")) {
            return new MockComponentSetter(mediator, test);
        }
        return null;
    }
}
