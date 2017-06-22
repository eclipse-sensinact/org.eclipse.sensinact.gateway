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

package org.eclipse.sensinact.gateway.app.manager;

import org.eclipse.sensinact.gateway.app.api.lifecycle.ApplicationStatus;

/**
 * This class defines the public constants used in the AppManager.
 *
 * @author Rémi Druilhe
 */
public class AppConstant {

    /***************************************
     * General constants of the AppManager
     ***************************************/

    /**
     * Name of the AppManager service provider
     */
    public static final String DEVICE_NAME = "AppManager";

    /************************************
     * Admin service property resources
     ************************************/

    /**
     * Display the available functions on the AppManager
     */
    public static final String KEYWORDS = "available_functions";

    /**********************************
     * Admin service action resources
     **********************************/

    /**
     * Enables to install an application
     */
    public static final String INSTALL = "INSTALL";

    /**
     * Enables to uninstall an application
     */
    public static final String UNINSTALL = "UNINSTALL";

    /*************************************
     * Application sensor data resources
     *************************************/

    /**
     * Store the lifecycle status of the application.
     *
     * @see ApplicationStatus
     */
    public static final String STATUS = "status";

    /**
     * Store the message associated to the current lifecycle status
     */
    public static final String STATUS_MESSAGE = "message";

    /**
     * Store the JSON content of the application.
     */
    public static final String CONTENT = "content";

    /********************************
     * Application action resources
     ********************************/

    /**
     * Enables to start an application
     */
    public static final String START = "START";

    /**
     * Enables to stop an application
     */
    public static final String STOP = "STOP";

    /**
     * Enables to throw an exception on an application
     * This action should only be used by the AppManager
     */
    public static final String EXCEPTION = "EXCEPTION";

    /**********************************
     * Application property resources
     **********************************/

    /**
     * An application stops when a resource disappears. If "true", the application
     * will be restarted when the resource re-appears.
     * Default is "false".
     */
    public static final String AUTORESTART = "autorestart";

    /**
     *
     * Default is "true".
     */
    public static final String RESET_ON_STOP = "resetOnStop";
}
