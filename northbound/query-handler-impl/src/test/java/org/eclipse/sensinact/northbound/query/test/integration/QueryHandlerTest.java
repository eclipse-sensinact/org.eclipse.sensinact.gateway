/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.query.test.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
public class QueryHandlerTest {

    @InjectService
    IQueryHandler handler;

    /**
     * Get the resource value
     */
    @Test
    void testLDAP() throws Exception {
        ICriterion filter = handler.parseFilter("(foo.bar=foobar)", "ldap");
        assertNotNull(filter);
        assertNotNull(filter.getResourceValueFilter());
    }

    /**
     * Get the resource value
     */
    @Test
    void testResourceSelector() throws Exception {
        ICriterion filter = handler.parseFilter("""
                { "service": {
                    "type": "EXACT",
                    "value": "sensor"
                  },
                  "resource": {
                    "type": "EXACT",
                    "value": "temperature"
                  },
                  "value": {
                    "value": "10",
                    "operation": "EQUALS"
                  }
                }""", "resource.selector");
        assertNotNull(filter);
        assertNotNull(filter.getResourceValueFilter());
    }
}
