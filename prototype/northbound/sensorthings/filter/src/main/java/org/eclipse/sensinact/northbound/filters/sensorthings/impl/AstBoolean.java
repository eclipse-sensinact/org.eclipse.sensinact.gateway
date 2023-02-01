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
public class AstBoolean extends AbstractAstPrimitive {
    private final boolean value;

    public AstBoolean(final boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    @Override
    public String toString() {
        return "Bool(" + value + ")";
    }
}
