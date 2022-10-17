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

/**
 * This class defines the fields defined in the JSON components to create an application
 */
public class AppJsonConstant {
    /*****************************
     * General constants section *
     *****************************/
    public static final String VALUE = "value";
    public static final String TYPE = "type";
    public static final String TYPE_VARIABLE = "variable";
    public static final String TYPE_RESOURCE = "resource";
    public static final String TYPE_EVENT = "event";
    public static final String URI_SEPARATOR = "/";
    /**********************
     * Initialize section *
     **********************/
    public static final String INITIALIZE = "initialize";
    public static final String INIT_OPTIONS = "options";
    public static final String INIT_OPTIONS_AUTORESTART = "autostart";
    public static final String INIT_OPTIONS_MAXINSTANCES = "maxinstances";
    public static final String INIT_OPTIONS_RESETONSTOP = "resetonstop";
    /***********************
     * Application section *
     ***********************/
    public static final String APPLICATION = "application";
    public static final String APP_IDENTIFIER = "identifier";
    public static final String APP_EVENTS = "events";
    public static final String APP_EVENTS_CONDITIONS = "conditions";
    public static final String APP_EVENTS_CONDITION_OPERATOR = "operator";
    public static final String APP_EVENTS_CONDITION_COMPLEMENT = "complement";
    public static final String APP_FUNCTION = "function";
    public static final String APP_FUNCTION_NAME = "name";
    public static final String APP_FUNCTION_BUILD_PARAMETERS = "buildparameters";
    public static final String APP_FUNCTION_RUN_PARAMETERS = "runparameters";
    public static final String APP_PROPERTIES = "properties";
    public static final String APP_PROPERTIES_REGISTER = "register";
    /********************
     * Finalize section *
     ********************/
    public static final String FINALIZE = "finalize";
}
