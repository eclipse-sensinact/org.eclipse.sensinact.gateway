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
package org.eclipse.sensinact.gateway.core.method.trigger;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Extended {@link AccessMethodTrigger} returning the value of the
 * Primitive parameter when executed
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Copy implements AccessMethodTrigger<Object[]> {
    public static final String NAME = "COPY";
    /**
     * the index of the execution parameter
     * to copy
     */
    private final int index;

    private final boolean doPassOn;

    /**
     * Constructor
     *
     * @param index the index of the execution parameter
     *              to copy
     */
    public Copy(int index, boolean doPassOn) {
        this.index = index;
        this.doPassOn = doPassOn;
    }

    /**
     * @inheritDoc
     * @see Executable#execute(java.lang.Object)
     */
    @Override
    public Object execute(Object[] parameters) throws Exception {
        return parameters[this.index];
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    @Override
    public String getJSON() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(JSONUtils.OPEN_BRACE);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(TRIGGER_TYPE_KEY);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(JSONUtils.COLON);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(this.getName());
        buffer.append(JSONUtils.QUOTE);
        buffer.append(JSONUtils.COMMA);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(TRIGGER_PASS_ON);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(JSONUtils.COLON);
        buffer.append(this.doPassOn);
        buffer.append(JSONUtils.COMMA);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(TRIGGER_INDEX_KEY);
        buffer.append(JSONUtils.QUOTE);
        buffer.append(JSONUtils.COLON);
        buffer.append(this.index);
        buffer.append(JSONUtils.CLOSE_BRACE);
        return buffer.toString();
    }

    /**
     * @inheritDoc
     * @see AccessMethodTrigger#getName()
     */
    @Override
    public String getName() {
        return Copy.NAME;
    }

    /**
     * @inheritDoc
     * @see AccessMethodTrigger#getParameters()
     */
    @Override
    public AccessMethodTrigger.Parameters getParameters() {
        return AccessMethodTrigger.Parameters.PARAMETERS;
    }

    /**
     * @inheritDoc
     * @see AccessMethodTrigger#passOn()
     */
    @Override
    public boolean passOn() {
        return this.doPassOn;
    }
}
