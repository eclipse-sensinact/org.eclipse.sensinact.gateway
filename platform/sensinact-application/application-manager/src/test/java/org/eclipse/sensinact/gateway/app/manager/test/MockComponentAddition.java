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
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.manager.osgi.AppServiceMediator;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

class MockComponentAddition extends AbstractFunction<Integer> {
    private static final String JSON_SCHEMA = "mock_addition.json";
    private final AppServiceMediator mediator;

    MockComponentAddition(AppServiceMediator mediator) {
        this.mediator = mediator;
    }

    static JSONObject getJSONSchemaFunction(BundleContext context) {
        try {
            return new JSONObject(new JSONTokener(new InputStreamReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void process(List<DataItf> variables) {
        super.update((Integer) CastUtils.cast(mediator.getClassLoader(), variables.get(0).getType(), variables.get(0).getValue()) + (Integer) CastUtils.cast(mediator.getClassLoader(), variables.get(1).getType(), variables.get(1).getValue()));
    }
}
