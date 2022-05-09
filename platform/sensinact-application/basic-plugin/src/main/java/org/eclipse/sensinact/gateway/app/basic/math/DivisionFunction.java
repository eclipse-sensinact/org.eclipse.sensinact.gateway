/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.math;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the division function
 *
 * @author Remi Druilhe
 * @see MathFunction
 */
public class DivisionFunction extends MathFunction<Double> {
	
	private static final Logger LOG = LoggerFactory.getLogger(DivisionFunction.class);
    private static final String JSON_SCHEMA = "division.json";

    public DivisionFunction() {
        super();
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
            double numerator = CastUtils.cast(double.class, datas.get(0).getValue());
            double divisor = CastUtils.cast(double.class, datas.get(1).getValue());
            if (divisor != 0) {
                result = numerator / divisor;
                if (LOG.isDebugEnabled()) {
                    LOG.debug(numerator + " / " + divisor + " = " + result);
                }
            } else {
                result = Double.NaN;
            }
        } catch (ClassCastException e) {
            result = Double.NaN;
            LOG.error(e.getMessage(), e);
        }
        super.update(result);
    }
}
