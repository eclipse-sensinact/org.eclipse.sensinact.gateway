/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/

package org.eclipse.sensinact.gateway.southbound.wot.http;

public class WhiteboardHandlerConfiguration {

    /**
     * Name of a key holding the argument(s) of an action. Use null for a direct
     * value.
     *
     * Useful for Things requiring an "input" field to hold the arguments
     */
    public String argumentsKey;

    /**
     * Should we send an empty map or a map with the argument key when action
     * arguments are empty
     */
    public boolean useArgumentsKeyOnEmptyArgs = false;

    /**
     * Name of the action result key
     *
     * Useful for Things returning results in a "result" field
     */
    public String resultKey;

    /**
     * Name of the key holding a property value. Set to null for a direct value.
     */
    public String propertyKey;

    /**
     * Name of the key holding a property timestamp. Set to null to ignore. Ignored
     * if {@link #propertyKey} is null.
     */
    public String timestampKey;
}
