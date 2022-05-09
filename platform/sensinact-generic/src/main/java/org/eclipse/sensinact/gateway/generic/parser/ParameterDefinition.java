/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.Fixed;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.ResourceImpl;
import org.eclipse.sensinact.gateway.core.ServiceImpl;
import org.eclipse.sensinact.gateway.core.method.DynamicParameter;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.method.builder.DynamicParameterValueFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Extended {@link ResolvedNameTypeValueDefinition} for parameter
 * XML node *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlEscaped(value = {"constraints"})
public class ParameterDefinition extends ResolvedNameTypeValueDefinition implements ConstrainableDefinition {
	
	private static final Logger LOG = LoggerFactory.getLogger(ParameterDefinition.class);
    
	private LinkedList<ConstraintDefinition> constraintDefinitions;
    private ParameterBuilderDefinition builder;
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
     * @param resource 
     *
     * @return the {@link Parameter} described by this
     * ParameterDefinition
     * @throws InvalidValueException
     */
    public Parameter getParameter(ResourceImpl resource, ServiceImpl service) throws InvalidValueException {
        Parameter parameter = null;
        try {
            if (builder != null) {
                DynamicParameterValue dynamic = null;
                DynamicParameterValueFactory.Loader loader = DynamicParameterValueFactory.LOADER.get();
                try {
                    DynamicParameterValueFactory factory = loader.load(mediator, builder.getName());
                    JSONObject builderDefinitionJSON = new JSONObject(builder.getJSON());
                    JSONObject builderJSON = builderDefinitionJSON.getJSONObject(DynamicParameterValue.BUILDER_KEY);
                    String reference = builder.getReference();
                    Executable<Void,Object> valueExtractor = null;
                    if(reference.startsWith("./")) {
                    	String attributeReference = reference.substring(2);
                    	valueExtractor = resource.getResourceValueExtractor(attributeReference);
                    } else {
                    	valueExtractor = service.getResourceValueExtractor(builder.getReference());
                    }
                    dynamic = factory.newInstance(mediator, valueExtractor, builderJSON);
                } finally {
                    DynamicParameterValueFactory.LOADER.remove();
                }
                parameter = new DynamicParameter(mediator, name, super.getType(), dynamic);
                return parameter;
            }
            Object fixedValue = null;
            Iterator<ConstraintDefinition> iterator = this.constraintDefinitions.iterator();
            List<Constraint> constraints = new ArrayList<Constraint>();

            while (iterator.hasNext()) {
                try {
                    Constraint constraint = iterator.next().getConstraint(super.getType());

                    if (Fixed.class.isAssignableFrom(constraint.getClass())) {
                        fixedValue = ((Fixed) constraint).getValue();
                        break;
                    }
                    constraints.add(constraint);
                } catch (InvalidConstraintDefinitionException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error( e.getMessage(), e);
                    }
                }
            }
            if (fixedValue == null) {
                parameter = new Parameter(super.mediator, super.getName(), super.getType(), constraints);
            } else {
                parameter = new Parameter(super.mediator, super.getName(), super.getType(), fixedValue);
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameter;
    }

    /**
     * @inheritDoc
     * 
     * @see org.eclipse.sensinact.gateway.generic.parser.ConstrainableDefinition#addConstraint(org.eclipse.sensinact.gateway.generic.parser.ConstraintDefinition)
     */
    public void addConstraint(ConstraintDefinition constraint) {
        if (Fixed.class.isAssignableFrom(constraint.getClass())) {
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
    public void setBuilder(ParameterBuilderDefinition builder) {
        this.fixed = true;
        this.builder = builder;
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
    public ParameterBuilderDefinition getBuilder() {
        return this.builder;
    }
    
    /**
     * @param atts
     */
    public void builderStart(Attributes atts) {
    	ParameterBuilderDefinition builder = new ParameterBuilderDefinition(mediator, getName(), atts);
    	this.builder = builder;
    	super.setNext(builder);
    }
}
