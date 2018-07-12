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
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.Fixed;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.method.DynamicParameter;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.builder.DynamicParameterValueFactory;
import org.json.JSONObject;
import org.xml.sax.Attributes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Extended {@link NameTypeValueDefinition} for parameter
 * xml element
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ParameterDefinition extends NameTypeValueDefinition implements ConstrainableDefinition {
    private LinkedList<ConstraintDefinition> constraintDefinitions;
    private BuilderDefinition builder;
    private boolean fixed = false;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml parameter element
     */
    ParameterDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
        this.constraintDefinitions = new LinkedList<ConstraintDefinition>();
    }

    /**
     * Returns the {@link TypeDefinition} of this
     * ParameterDefinition
     *
     * @return the {@link TypeDefinition} of this
     * ParameterDefinition
     */
    public TypeDefinition getType() {
        return super.getTypeDefinition();
    }

    /**
     * Returns true if a {@link Parameter} based on
     * this ParameterDefinition is modifiable or not ;
     * otherwise returns false
     *
     * @return true if a {@link Parameter} based on this
     * ParameterDefinition is modifiable ;false otherwise
     */
    public boolean isModifiable() {
        return !this.fixed;
    }

    /**
     * Creates and returns the {@link Parameter} described by
     * this ParameterDefinition
     *
     * @return the {@link Parameter} described by this
     * ParameterDefinition
     * @throws InvalidValueException
     */
    public Parameter getParameter(ServiceImpl service) throws InvalidValueException {
        Parameter parameter = null;
        try {
            TypeDefinition typeDefinition = super.getTypeDefinition();

            if (typeDefinition == null) {
                if (super.mediator.isErrorLoggable()) {
                    super.mediator.error("no defined type : " + "unable to create the Parameter");
                }
                return parameter;
            }
            if (builder != null) {
                DynamicParameterValue dynamic = null;
                DynamicParameterValueFactory.Loader loader = DynamicParameterValueFactory.LOADER.get();
                try {
                    DynamicParameterValueFactory factory = loader.load(mediator, builder.getName());
                    JSONObject builderDefinitionJSON = new JSONObject(builder.getJSON());
                    JSONObject builderJSON = builderDefinitionJSON.getJSONObject(DynamicParameterValue.BUILDER_KEY);
                    dynamic = factory.newInstance(mediator, service.getResourceValueExtractor(builder.getReference()), builderJSON);
                } finally {
                    DynamicParameterValueFactory.LOADER.remove();
                }
                parameter = new DynamicParameter(mediator, name, typeDefinition.getType(), dynamic);

                return parameter;
            }
            Object fixedValue = null;
            Iterator<ConstraintDefinition> iterator = this.constraintDefinitions.iterator();
            Set<Constraint> constraints = new HashSet<Constraint>();

            while (iterator.hasNext()) {
                try {
                    Constraint constraint = iterator.next().getConstraint(typeDefinition.getType());

                    if (Fixed.class.isAssignableFrom(constraint.getClass())) {
                        fixedValue = ((Fixed) constraint).getValue();
                        break;
                    }
                    constraints.add(constraint);
                } catch (InvalidConstraintDefinitionException e) {
                    if (super.mediator.isErrorLoggable()) {
                        super.mediator.error(e, e.getMessage());
                    }
                }
            }
            if (fixedValue == null) {
                parameter = new Parameter(super.mediator, super.getName(), typeDefinition.getType(), (Set<Constraint>) constraints);
            } else {
                parameter = new Parameter(super.mediator, super.getName(), typeDefinition.getType(), fixedValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameter;
    }

    /**
     * @inheritDoc
     * @see ConstrainableDefinition#
     * addConstraint(ConstraintDefinition)
     */
    public void addConstraint(ConstraintDefinition constraint) {
        if (constraint != null && Fixed.class.isAssignableFrom(constraint.getClass())) {
            fixed = true;
        }
        this.constraintDefinitions.add(constraint);
    }

    /**
     * Defines the BuilderDefinition describing the {@link
     * DynamicParameterValue} used to build the dynamic value
     * of the {@link DynamicParameter} described by this
     * ParameterDefinition
     *
     * @param builder the BuilderDefinition describing the {@link
     *                DynamicParameterValue} of the described {@link
     *                DynamicParameter}
     */
    public void setBuilder(BuilderDefinition builder) {
        if (builder != null) {
            this.fixed = true;
            this.builder = builder;
        }
    }

    /**
     * Returns the BuilderDefinition describing the {@link
     * DynamicParameterValue} used to build the dynamic value
     * of the {@link DynamicParameter} described by this
     * ParameterDefinition
     *
     * @return the BuilderDefinition describing the {@link
     * DynamicParameterValue} of the described {@link
     * DynamicParameter}
     */
    public BuilderDefinition getBuilder() {
        return this.builder;
    }
}
