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
package org.eclipse.sensinact.gateway.app.basic.math;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * This class implements the subtraction function
 *
 * @author Remi Druilhe
 * @see MathFunction
 */
public class SubtractionFunction extends MathFunction<Double> {
    private static final String JSON_SCHEMA = "subtraction.json";

    public SubtractionFunction(Mediator mediator) {
        super(mediator);
    }

    /**
     * Gets the JSON schema of the function from the plugin
     *
     * @param context the context of the bundle
     * @return the JSON schema of the function
     */
    public static JSONObject getJSONSchemaFunction(BundleContext context) {
        try {
            return new JSONObject(new JSONTokener(new InputStreamReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see AbstractFunction#process(List)
     */
    public void process(List<DataItf> datas) {
        double result;
        try {
            double firstOperand = CastUtils.cast(super.mediator.getClassLoader(), double.class, datas.get(0).getValue());
            double secondOperand = CastUtils.cast(super.mediator.getClassLoader(), double.class, datas.get(1).getValue());
            result = firstOperand - secondOperand;
            if (mediator.isDebugLoggable()) {
                mediator.debug(firstOperand + " - " + secondOperand + " = " + result);
            }
        } catch (ClassCastException e) {
            result = Double.NaN;
            mediator.error(e.getMessage(), e);
        }
        super.update(result);
    }
}
