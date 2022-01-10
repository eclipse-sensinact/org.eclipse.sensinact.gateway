/*
* Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.system.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.method.DescribeResponse;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class TestSensiNactResource {


    @Test
    public void testSensiNactResource(@InjectService(timeout = 500) Core core) throws Throwable {
    	
    	Session session = core.getAnonymousSession();
    	
    	DescribeResponse<JSONObject> provider = session.getProvider("sensiNact");
    	
    	assertEquals("sensiNact", provider.getResponse().getString("name"));
    	
    }
    
}