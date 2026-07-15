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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static org.eclipse.sensinact.sensorthings.sensing.rest.IFilterConstants.EXPAND_SETTINGS_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpandFilterTest extends QueryOptionFilterTestSupport {

    private ExpandFilter filter;

    @BeforeEach
    void setUpFilter() {
        filter = new ExpandFilter();
    }

    private ExpansionSettingsImpl expansionSettings() {
        Object settings = requestContext.getProperty(EXPAND_SETTINGS_STRING);
        assertNotNull(settings);
        return (ExpansionSettingsImpl) settings;
    }

    @Test
    void missingExpandStoresEmptySettings() throws IOException {
        filter.filter(requestContext);

        assertTrue(expansionSettings().isEmpty());
    }

    @Test
    void expandParameterStoresNonEmptySettings() throws IOException {
        queryParameters.putSingle("$expand", "Datastreams");

        filter.filter(requestContext);

        assertFalse(expansionSettings().isEmpty());
    }

    @Test
    void nestedExpandClausesAreSplitOnTopLevelCommasOnly() {
        List<String> parts = ExpansionSettingsImpl.split("Datastreams($expand=Observations,Sensor),Locations")
                .toList();

        assertEquals(List.of("Datastreams($expand=Observations,Sensor)", "Locations"), parts);
    }

    @Test
    void emptySettingsLeaveResponseUntouched() throws IOException {
        filter.filter(requestContext);

        ResultList<TestEntity> original = new ResultList<>(entities(2));
        setResponseEntity(original);
        filter.filter(requestContext, responseContext);

        assertSame(original, responseEntity());
    }
}
