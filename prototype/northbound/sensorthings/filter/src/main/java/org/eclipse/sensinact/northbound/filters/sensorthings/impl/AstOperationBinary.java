/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.filters.sensorthings.impl;

/**
 * @author thoma
 *
 */
public class AstOperationBinary extends AstOperation {

    private final AstPrimitive left;
    private final AstPrimitive right;

    public AstOperationBinary(Operator operator, AstPrimitive left, AstPrimitive right) {
        super(operator);
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "Binary(" + operator + ", " + left + ", " + right + ")";
    }
}
