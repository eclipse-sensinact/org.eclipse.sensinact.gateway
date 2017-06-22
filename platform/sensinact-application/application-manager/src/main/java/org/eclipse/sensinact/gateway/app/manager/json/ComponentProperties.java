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
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONObject;

/**
 * This class is a builder gathering the properties that are applied on an component
 *
 * @author Remi Druilhe
 */
public class ComponentProperties implements JSONable {

    private static final boolean DEFAULT_REGISTER_VALUE = false;

    private final boolean register;

    /**
     * Constructor
     * @param builder the builder from which the ComponentProperties object is created
     */
    private ComponentProperties(Builder builder) {
        this.register = builder.register;
    }

    /**
     * If true, the component output is registered as a new sNa resource
     * @return the register property
     */
    public boolean getRegister() {
        return register;
    }

    /**
     * The builder to create the AppOptions object
     */
    public static class Builder {

        private boolean register = DEFAULT_REGISTER_VALUE;

        /**
         * A component can register its output as a new sNa resource
         * @param register the register value
         * @return the builder
         */
        public Builder register(boolean register) {
            this.register = register;
            return this;
        }

        /**
         * Create the ComponentProperties object
         * @return the ComponentProperties object builds from the builder
         */
        public ComponentProperties build() {
            return new ComponentProperties(this);
        }
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return new JSONObject().put(AppJsonConstant.APP_PROPERTIES_REGISTER, register).toString();
    }
}
