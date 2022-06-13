/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

/**
 * This class is a builder gathering the options that are applied on an application
 *
 * @author Remi Druilhe
 */
public class AppOptions implements JSONable {
    private static final boolean DEFAULT_AUTORESTART_VALUE = false;
    private static final boolean DEFAULT_RESET_ON_STOP_VALUE = true;
    private final boolean autostart;
    private final boolean resetOnStop;

    /**
     * Constructor
     *
     * @param builder the builder from which the AppOptions object is created
     */
    private AppOptions(Builder builder) {
        this.autostart = builder.autorestart;
        this.resetOnStop = builder.resetOnStop;
    }

    /**
     * Get the autostart value
     *
     * @return the autostart value
     */
    public boolean getAutoStart() {
        return autostart;
    }

    /**
     * Get the resetOnStop value
     *
     * @return the resetOnStop value
     */
    public boolean getResetOnStop() {
        return resetOnStop;
    }

    /**
     * The builder to create the AppOptions object
     */
    public static class Builder {
        private boolean autorestart = DEFAULT_AUTORESTART_VALUE;
        private boolean resetOnStop = DEFAULT_RESET_ON_STOP_VALUE;

        /**
         * An application stops when a resource disappears. If "true", the application
         * will be restarted when the resource re-appears.
         *
         * @param autorestart the autostart value
         * @return the builder
         */
        Builder autorestart(boolean autorestart) {
            this.autorestart = autorestart;
            return this;
        }

        /**
         * @param resetOnStop the resetOnStop value
         * @return the builder
         */
        Builder resetOnStop(boolean resetOnStop) {
            this.resetOnStop = resetOnStop;
            return this;
        }

        /**
         * Create the AppOptions object
         *
         * @return the AppOptions object builds from the builder
         */
        public AppOptions build() {
            return new AppOptions(this);
        }
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return JsonProviderFactory.getProvider().createObjectBuilder()
        		.add(AppJsonConstant.INIT_OPTIONS_AUTORESTART, autostart)
        		.add(AppJsonConstant.INIT_OPTIONS_RESETONSTOP, resetOnStop).build().toString();
    }
}
