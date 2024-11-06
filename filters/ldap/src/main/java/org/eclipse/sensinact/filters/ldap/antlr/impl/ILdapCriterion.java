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
package org.eclipse.sensinact.filters.ldap.antlr.impl;

import org.eclipse.sensinact.core.snapshot.ICriterion;

public interface ILdapCriterion extends ICriterion {
    /**
     * Negate the current criterion
     */
    ILdapCriterion negate();
}
