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
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;

/**
 * Wraps a subscription condition
 *
 * @author Remi Druilhe
 */
public class AppCondition implements JSONable {
    private final Mediator mediator;
    private final String operator;
    private final AppParameter parameter;
    private final boolean complement;

    /**
     * Creates a condition
     *
     * @param mediator   the mediator
     * @param operator   the operator of the constraint
     * @param parameter  the parameter of the constraint
     * @param complement the complement of the constraint
     */
    public AppCondition(Mediator mediator, String operator, AppParameter parameter, boolean complement) {
        this.mediator = mediator;
        this.operator = operator;
        this.parameter = parameter;
        this.complement = complement;
    }

    /**
     * JSON constructor to create an application
     *
     * @param mediator  the mediator
     * @param condition the JSON version of the condition
     */
    public AppCondition(Mediator mediator, JSONObject condition) {
        this(mediator, condition.getString(AppJsonConstant.APP_EVENTS_CONDITION_OPERATOR), new AppParameter(condition.get(AppJsonConstant.VALUE), condition.getString(AppJsonConstant.TYPE)), condition.optBoolean(AppJsonConstant.APP_EVENTS_CONDITION_COMPLEMENT));
    }

    /**
     * Get the constraint
     *
     * @return the constraint
     */
    public Constraint getConstraint() {
        try {
            return ConstraintFactory.Loader.load(mediator.getClassLoader(), AppOperator.getOperator(operator), CastUtils.jsonTypeToJavaType(parameter.getType()), parameter.getValue(), complement);
        } catch (InvalidConstraintDefinitionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppCondition that = (AppCondition) o;
        if (complement != that.complement) {
            return false;
        }
        if (!operator.equals(that.operator)) {
            return false;
        }
        return parameter.equals(that.parameter);
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        int result = operator.hashCode();
        result = 31 * result + parameter.hashCode();
        result = 31 * result + (complement ? 1 : 0);
        return result;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return new JSONObject().put(AppJsonConstant.APP_EVENTS_CONDITION_OPERATOR, operator).put(AppJsonConstant.VALUE, parameter.getValue()).put(AppJsonConstant.TYPE, parameter.getType()).put(AppJsonConstant.APP_EVENTS_CONDITION_COMPLEMENT, complement).toString();
    }
}
