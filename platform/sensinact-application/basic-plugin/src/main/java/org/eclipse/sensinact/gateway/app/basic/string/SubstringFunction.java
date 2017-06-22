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

package org.eclipse.sensinact.gateway.app.basic.string;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class implements the substring function
 *
 * @see StringFunction
 *
 * @author Remi Druilhe
 */
public class SubstringFunction extends StringFunction<String> {

    private final Mediator mediator;
    private static final String JSON_SCHEMA = "substring.json";

    public SubstringFunction(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Gets the JSON schema of the function from the plugin
     * @param context the context of the bundle
     * @return the JSON schema of the function
     */
    public static JSONObject getJSONSchemaFunction(BundleContext context) {
        try {
            return new JSONObject(new JSONTokener(
                    new InputStreamReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @see AbstractFunction#process(List)
     */
    public void process(List<DataItf> variables) {
        if(variables.size() == 2) {
            super.update(CastUtils.cast(mediator.getClassLoader(),
                    String.class, variables.get(0).getValue())
                    .substring(CastUtils.castPrimitive(int.class, variables.get(1).getValue())));

            return;
        } else if(variables.size() == 3) {
            super.update(CastUtils.cast(mediator.getClassLoader(),
                    String.class, variables.get(0).getValue())
                    .substring(CastUtils.castPrimitive(int.class, variables.get(1).getValue()),
                            CastUtils.castPrimitive(int.class, variables.get(2).getValue())));

            return;
        }

        super.update(null);
    }
}
