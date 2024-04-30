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
package org.eclipse.sensinact.core.model;

/**
 * A common super-interface for modelled types
 *
 * @author timothyjward
 *
 */
public interface Modelled {

    String getName();

    boolean isExclusivelyOwned();

    boolean isAutoDelete();

    /**
     * Indicates if this is loaded Model, which is not allowed to be changed. Any
     * changes to this model will result in an Exception. TODO: What kind of
     * Exception?
     */
    boolean isFrozen();
}
