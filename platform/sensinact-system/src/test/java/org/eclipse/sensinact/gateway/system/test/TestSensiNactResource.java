/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.system.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.json.JsonObject;

@ExtendWith(ServiceExtension.class)
public class TestSensiNactResource {


    @Test
    public void testSensiNactResource(@InjectService(timeout = 500) Core core) throws Throwable {
    	Thread.sleep(1000);
    	Session session = core.getAnonymousSession();
    	
    	DescribeResponse<JsonObject> provider = session.getProvider("sensiNact");
    	
    	assertEquals("sensiNact", provider.getResponse().getString("name"));
    	
    }
    
}