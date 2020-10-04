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
package org.eclipse.sensinact.gateway.generic.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.xml.sax.Attributes;

/**
 * Extended {@link XmlDefinition} describing a trigger executed
 * when an associated ActionResource is invocated
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@XmlAttributes({@XmlAttribute(attribute = "passOn", field = "passOn")})
public class ReferenceDefinition extends BuilderDefinition {
	
	private TriggerBuilderDefinition builder;
    private boolean passOn;

    /**
     * Constructor
     *
     * @param mediator the associated Mediator
     * @param atts     the set of attributes data structure for the
     *                 xml reference element
     */
    ReferenceDefinition(Mediator mediator, Attributes atts) {
        super(mediator, atts);
    }


    /**
     * Defines whether the {@link AccessMethodTrigger} described
     * by this {@link ReferenceDefinition} triggers the update
     * to upper layers or not
     *
     * @param passOn <ul>
     *               <li>true to pass on the change to upper layer</li>
     *               <li>false otherwise</li>
     *               </ul>
     */
    public void setPassOn(String passOn) {
        this.passOn = Boolean.parseBoolean(passOn);
    }

    /**
     * Returns true if the {@link AccessMethodTrigger} described
     * by this {@link ReferenceDefinition} triggers the update
     * to upper layers; returns false otherwise
     *
     * @return <ul>
     * <li>true to pass on the change to upper layer</li>
     * <li>false otherwise</li>
     * </ul>
     */
    public boolean getPassOn() {
        return this.passOn;
    }

    /**
     * Returns the name of the type of the {@link DynamicParameterValue}
     * described by this BuilderDefinition
     *
     * @return the name of the type of the described {@link
     * DynamicParameterValue}
     */
    public String getName() {
    	String name = super.getName().toUpperCase();
    	name = name.substring(0,name.length()-9);
        if ("CALCULATED".equals(name)) {
            return super.getSubType();
        }
        return name;
    }

    /**
     * Returns the argument builder of the {@link AccessMethodTrigger} described by this 
     * ReferenceDefinition
     *
     * @return the {@link AccessMethodTrigger} argument builder
     */
    public TriggerBuilderDefinition getTriggerBuilderDefinition() {
        return this.builder;
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();

        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append("reference");
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(super.getReference());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.OPEN_BRACE);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_TYPE_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(JSONUtils.QUOTE);
        builder.append(getName());
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COMMA);
        builder.append(JSONUtils.QUOTE);
        builder.append(AccessMethodTrigger.TRIGGER_PASSON_KEY);
        builder.append(JSONUtils.QUOTE);
        builder.append(JSONUtils.COLON);
        builder.append(this.passOn);
        if(this.builder!=null) {
        	builder.append(JSONUtils.COMMA);
        	builder.append(this.builder.getJSON());
        }
        builder.append(JSONUtils.CLOSE_BRACE);
        builder.append(JSONUtils.CLOSE_BRACE);
        return builder.toString();
    }
    
    /**
     * @param atts
     */
    public void builderStart(Attributes atts) {
    	 TriggerBuilderDefinition builder = new TriggerBuilderDefinition(mediator, atts);
    	 this.builder = builder;
    	 super.setNext(builder);
    }
   
}
