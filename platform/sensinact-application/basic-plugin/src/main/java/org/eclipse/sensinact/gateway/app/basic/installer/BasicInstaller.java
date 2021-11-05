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
package org.eclipse.sensinact.gateway.app.basic.installer;

import org.eclipse.sensinact.gateway.app.api.function.AbstractFunction;
import org.eclipse.sensinact.gateway.app.api.plugin.AbstractPlugin;
import org.eclipse.sensinact.gateway.app.api.plugin.PluginInstaller;
import org.eclipse.sensinact.gateway.app.basic.logic.BetweenFunction;
import org.eclipse.sensinact.gateway.app.basic.logic.DoubleConditionFunction;
import org.eclipse.sensinact.gateway.app.basic.logic.SimpleConditionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.AdditionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.AssignmentFunction;
import org.eclipse.sensinact.gateway.app.basic.math.DivisionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.MathFunction;
import org.eclipse.sensinact.gateway.app.basic.math.ModuloFunction;
import org.eclipse.sensinact.gateway.app.basic.math.MultiplicationFunction;
import org.eclipse.sensinact.gateway.app.basic.math.SubtractionFunction;
import org.eclipse.sensinact.gateway.app.basic.sna.ActActionFunction;
import org.eclipse.sensinact.gateway.app.basic.sna.ActionFunction;
import org.eclipse.sensinact.gateway.app.basic.sna.SetActionFunction;
import org.eclipse.sensinact.gateway.app.basic.string.ConcatenateFunction;
import org.eclipse.sensinact.gateway.app.basic.string.StringFunction;
import org.eclipse.sensinact.gateway.app.basic.string.SubstringFunction;
import org.eclipse.sensinact.gateway.app.basic.time.SleepFunction;
import org.eclipse.sensinact.gateway.app.basic.time.TimeFunction;
import org.eclipse.sensinact.gateway.app.manager.json.AppFunction;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.json.JSONObject;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @see PluginInstaller
 */
@Component(immediate = true, service = PluginInstaller.class, property = {"plugin.name=BasicPlugin"})
public class BasicInstaller extends AbstractPlugin {
	
    protected Mediator mediator;

    @Activate
    public void activate(ComponentContext context) {
    	this.mediator = new Mediator(context.getBundleContext()); 
    }

    /**
     * @see PluginInstaller#getComponentJSONSchema(String)
     */
    public JSONObject getComponentJSONSchema(String function) {
        if (function.equals("lesserThan") || function.equals("lesserEqual") || function.equals("greaterThan") || function.equals("greaterEqual") || function.equals("equal") || function.equals("diff") || function.equals("regex") || function.equals("abs") || function.equals("delta")) {
            return SimpleConditionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (function.equals("and") || function.equals("or")) {
            return DoubleConditionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (function.equals("in")) {
            return BetweenFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (MathFunction.MathOperator.ADDITION.getOperator().equalsIgnoreCase(function)) {
            return AdditionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (MathFunction.MathOperator.SUBTRACTION.getOperator().equalsIgnoreCase(function)) {
            return SubtractionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (MathFunction.MathOperator.MULTIPLICATION.getOperator().equalsIgnoreCase(function)) {
            return MultiplicationFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (MathFunction.MathOperator.DIVISION.getOperator().equalsIgnoreCase(function)) {
            return DivisionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (MathFunction.MathOperator.MODULO.getOperator().equalsIgnoreCase(function)) {
            return ModuloFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (MathFunction.MathOperator.ASSIGNMENT.getOperator().equalsIgnoreCase(function)) {
            return AssignmentFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (ActionFunction.SnaOperator.ACT.getOperator().equalsIgnoreCase(function)) {
            return ActActionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (ActionFunction.SnaOperator.SET.getOperator().equalsIgnoreCase(function)) {
            return SetActionFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (StringFunction.StringOperator.CONCATENATE.getOperator().equalsIgnoreCase(function)) {
            return ConcatenateFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (StringFunction.StringOperator.SUBSTRING.getOperator().equalsIgnoreCase(function)) {
            return SubstringFunction.getJSONSchemaFunction(mediator.getContext());
        } else if (TimeFunction.TimeOperator.SLEEP.getOperator().equalsIgnoreCase(function)) {
            return SleepFunction.getJSONSchemaFunction(mediator.getContext());
        }
        //TODO
        return null;
    }

    /**
     * @see PluginInstaller#getFunction(AppFunction)
     */
    public AbstractFunction<?> getFunction(AppFunction function) {
        String functionName = function.getName();
        if (functionName.equals("lesserThan") || functionName.equals("lesserEqual") || functionName.equals("greaterThan") || functionName.equals("greaterEqual") || functionName.equals("equal") || functionName.equals("diff") || functionName.equals("regex") || functionName.equals("abs") || functionName.equals("delta")) {
            return new SimpleConditionFunction(mediator.getClassLoader(), functionName);
        } else if (functionName.equals("and") || functionName.equals("or")) {
            return new DoubleConditionFunction(functionName);
        } else if (functionName.equals("in")) {
            return new BetweenFunction();
        } else if (MathFunction.MathOperator.ADDITION.getOperator().equalsIgnoreCase(functionName)) {
            return new AdditionFunction();
        } else if (MathFunction.MathOperator.SUBTRACTION.getOperator().equalsIgnoreCase(functionName)) {
            return new SubtractionFunction();
        } else if (MathFunction.MathOperator.MULTIPLICATION.getOperator().equalsIgnoreCase(functionName)) {
            return new MultiplicationFunction();
        } else if (MathFunction.MathOperator.DIVISION.getOperator().equalsIgnoreCase(functionName)) {
            return new DivisionFunction();
        } else if (MathFunction.MathOperator.MODULO.getOperator().equalsIgnoreCase(functionName)) {
            return new ModuloFunction();
        } else if (MathFunction.MathOperator.ASSIGNMENT.getOperator().equalsIgnoreCase(functionName)) {
            return new AssignmentFunction();
        } else if (ActionFunction.SnaOperator.ACT.getOperator().equalsIgnoreCase(functionName)) {
            return new ActActionFunction();
        } else if (ActionFunction.SnaOperator.SET.getOperator().equalsIgnoreCase(functionName)) {
            return new SetActionFunction();
        } else if (StringFunction.StringOperator.CONCATENATE.getOperator().equalsIgnoreCase(functionName)) {
            return new ConcatenateFunction();
        } else if (StringFunction.StringOperator.SUBSTRING.getOperator().equalsIgnoreCase(functionName)) {
            return new SubstringFunction();
        } else if (TimeFunction.TimeOperator.SLEEP.getOperator().equalsIgnoreCase(functionName)) {
            return new SleepFunction();
        }
        return null;
    }
}
