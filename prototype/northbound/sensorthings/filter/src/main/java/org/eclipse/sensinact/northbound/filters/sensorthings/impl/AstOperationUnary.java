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
public class AstOperationUnary extends AstOperation {

    private final AstPrimitive operand;

    public AstOperationUnary(Operator operator, AstPrimitive operand) {
        super(operator);
        this.operand = operand;
    }

    @Override
    public String toString() {
        return "Unary(" + operator + ", " + operand + ")";
    }
}
