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
public class AstNumber extends AbstractAstPrimitive {

    private final int intValue;
    private final double doubleValue;
    private final boolean isInt;

    public AstNumber(final int value) {
        this.doubleValue = 0;
        this.intValue = value;
        this.isInt = true;
    }

    public AstNumber(final double value) {
        this.intValue = 0;
        this.doubleValue = value;
        this.isInt = true;
    }

    public boolean isInt() {
        return isInt;
    }

    public int getInt() {
        return intValue;
    }

    public double getDouble() {
        return doubleValue;
    }

    @Override
    public String toString() {
        if (isInt) {
            return "Int(" + intValue + ")";
        } else {
            return "Double(" + doubleValue + ")";
        }
    }
}
