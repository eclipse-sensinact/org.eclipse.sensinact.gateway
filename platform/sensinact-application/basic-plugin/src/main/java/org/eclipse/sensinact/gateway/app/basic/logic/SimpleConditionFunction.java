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
package org.eclipse.sensinact.gateway.app.basic.logic;

import org.eclipse.sensinact.gateway.app.api.exception.NotAReadableResourceException;
import org.eclipse.sensinact.gateway.app.api.exception.ResourceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.exception.ServiceNotFoundException;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.manager.json.AppOperator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A component that tests if the simple condition is satisfied.
 *
 * @author Remi Druilhe
 * @see ConditionFunction
 */
public class SimpleConditionFunction extends ConditionFunction {
    private final Mediator mediator;
    private final String function;
    private static final String JSON_SCHEMA = "simple_condition.json";

    public SimpleConditionFunction(Mediator mediator, String function) {
        this.mediator = mediator;
        this.function = function;
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
     * @param datas the variables to tests
     * @throws NotAReadableResourceException
     * @throws ResourceNotFoundException
     * @throws ServiceNotFoundException
     * @see ConditionFunction#testCondition(List)
     */
    public Boolean testCondition(List<DataItf> datas) throws NotAReadableResourceException, ResourceNotFoundException, ServiceNotFoundException {
        DataItf variable = datas.get(0);
        DataItf value = datas.get(1);
        boolean complement = false;
        if (datas.size() > 2) {
            complement = CastUtils.castPrimitive(boolean.class, datas.get(2).getValue());
        }
        try {
            /*System.out.println(variable.getValue() + " (" + variable.getType().getCanonicalName() + ") "
                    + function + " " + value.getValue() + " (" + value.getType().getCanonicalName() + ")");*/
            Constraint constraint = ConstraintFactory.Loader.load(mediator.getClassLoader(), AppOperator.getOperator(function), value.getType(), value.getValue(), complement);
            return constraint.complies(variable.getValue());
        } catch (InvalidConstraintDefinitionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
