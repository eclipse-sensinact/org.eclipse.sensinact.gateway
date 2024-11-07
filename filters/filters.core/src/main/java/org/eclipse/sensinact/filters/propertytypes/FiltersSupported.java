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
package org.eclipse.sensinact.filters.propertytypes;

import org.osgi.service.component.annotations.ComponentPropertyType;

/**
 * Specification of a filter parser
 */
@ComponentPropertyType
public @interface FiltersSupported {

    /**
     * Set the full property name sensinact.filters.supported
     */
    String PREFIX_ = "sensinact.";

    String[] value();
}
