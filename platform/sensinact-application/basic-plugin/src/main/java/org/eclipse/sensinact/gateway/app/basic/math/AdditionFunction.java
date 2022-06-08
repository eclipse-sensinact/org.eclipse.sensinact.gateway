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
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;

/**
 * This class implements the addition function
 *
 * @author Remi Druilhe
 * @see MathFunction
 */
public class AdditionFunction extends MathFunction<Double> {
	
	private static final Logger LOG = LoggerFactory.getLogger(AdditionFunction.class);
    private static final String JSON_SCHEMA = "addition.json";

    public AdditionFunction() {
        super();
    }

    /**
     * Gets the JSON schema of the function from the plugin
     *
     * @param context the context of the bundle
     * @return the JSON schema of the function
     */
    public static JsonObject getJSONSchemaFunction(BundleContext context) {
        try {
        	return JsonProviderFactory.getProvider().createReader(context.getBundle().getResource("/" + JSON_SCHEMA).openStream()).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see AbstractFunction#process(List)
     */
    public void process(List<DataItf> datas) {
        double result = 0;
        int length = datas == null ? 0 : datas.size();
        if (length > 0) {
            try {
                result = CastUtils.cast(double.class, datas.get(0).getValue());
                for (int i = 1; i < length; i++) {
                    result = result + CastUtils.cast(double.class, datas.get(i).getValue());
                }
            } catch (ClassCastException e) {
                result = Double.NaN;
                LOG.error(e.getMessage(), e);
            }
        }
        super.update(result);
    }
}
