/*
 * Copyright 2024 Kentyou
 * Proprietary and confidential
 *
 * All Rights Reserved.
 * Unauthorized copying of this file is strictly prohibited
 */
package org.eclipse.sensinact.gateway.southbound.wot.http;

public @interface FormHandlerConfiguration {

    /**
     * Name of a key holding the argument(s) of an action. Use null for a direct
     * value.
     *
     * Useful for Things requiring an "input" field to hold the arguments
     */
    String argumentsKey();

    /**
     * Should we send an empty map or a map with the argument key when action
     * arguments are empty
     */
    boolean useArgumentsKeyOnEmptyArgs() default false;

    /**
     * Name of the key holding a property value. Set to null for a direct value.
     */
    String propertyKey();

    /**
     * Name of the key holding a property timestamp. Set to null to ignore. Ignored
     * if {@link #propertyKey} is null.
     */
    String timestampKey();

    /**
     * Per-URL prefix configuration
     */
    String url_configuration();
}
