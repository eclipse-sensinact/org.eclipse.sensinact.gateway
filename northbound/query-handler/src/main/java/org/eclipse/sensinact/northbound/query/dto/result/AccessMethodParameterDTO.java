/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.query.dto.result;

/**
 * Represents an access method parameter
 *
 * FIXME: parameter name might be ignored in new model?
 */
public class AccessMethodParameterDTO {

    /**
     * Parameter name
     */
    public String name;

    /**
     * Parameter type: either the name of a primitive, "string" or a Java class name
     */
    public String type;

    /**
     * True if the parameter is forced
     */
    public boolean fixed;

    /**
     * Array of constraints
     */
    public String[] constraints = new String[0];
}
