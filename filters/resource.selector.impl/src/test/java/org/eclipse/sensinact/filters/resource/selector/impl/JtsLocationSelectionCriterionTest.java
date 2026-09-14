/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.resource.selector.impl;

import org.eclipse.sensinact.filters.location.api.LocationMatchFactory;
import org.eclipse.sensinact.gateway.filters.jts.geometry.JtsLocationMatchFactory;

/**
 * This test implementation tests the functionality with Eclipse JTS
 */
public class JtsLocationSelectionCriterionTest extends AbstractLocationSelectionCriterionTest {

    @Override
    protected LocationMatchFactory getLocationMatchFactory() {
        return new JtsLocationMatchFactory();
    }

}
