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
 * This class wraps the information for the initialization of the application
 *
 * @author Rémi Druilhe
 */
public class AppInitialize implements JSONable {
    private final AppOptions options;

    /**
     * Java constructor without options
     */
    public AppInitialize() {
        this(new AppOptions.Builder().build());
    }

    /**
     * Java constructor
     *
     * @param options the options of the application
     */
    public AppInitialize(AppOptions options) {
        this.options = options;
    }

    /**
     * JSON constructor
     *
     * @param initialize the initialize as JSON
     */
    public AppInitialize(JSONObject initialize) {
        AppOptions.Builder appOptionBuilder = new AppOptions.Builder();
        if (initialize.has(AppJsonConstant.INIT_OPTIONS)) {
            JSONObject optionsJson = initialize.getJSONObject(AppJsonConstant.INIT_OPTIONS);
            if (optionsJson.has(AppJsonConstant.INIT_OPTIONS_AUTORESTART)) {
                appOptionBuilder.autorestart(optionsJson.getBoolean(AppJsonConstant.INIT_OPTIONS_AUTORESTART));
            }
            if (optionsJson.has(AppJsonConstant.INIT_OPTIONS_RESETONSTOP)) {
                appOptionBuilder.resetOnStop(optionsJson.getBoolean(AppJsonConstant.INIT_OPTIONS_RESETONSTOP));
            }
        }
        this.options = appOptionBuilder.build();
    }

    /**
     * Gets the options of the application
     *
     * @return the options of the application
     */
    public AppOptions getOptions() {
        return options;
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        JSONObject initialize = new JSONObject();
        initialize.put(AppJsonConstant.INIT_OPTIONS, options.getJSON());
        return initialize.toString();
    }
}
