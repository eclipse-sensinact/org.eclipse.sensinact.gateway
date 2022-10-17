/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.test;

import java.util.Enumeration;

import org.junit.jupiter.api.BeforeAll;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationPlugin;
import org.osgi.test.common.annotation.InjectBundleContext;

public class AbstractConfiguredSecurityTest {

	@BeforeAll
	public static void setup(@InjectBundleContext BundleContext ctx) {
		ctx.registerService(ConfigurationPlugin.class, (ref, props) -> {
			
			Enumeration<String> keys = props.keys();
			
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				Object value = props.get(key);
				if(value instanceof String) {
					String s = value.toString();
					if(s.startsWith("${")) {
						props.put(key, System.getProperty(s.substring(2, s.length() -1), s));
					}
				}
			}
			
		}, null);
	}

	public AbstractConfiguredSecurityTest() {
		super();
	}

}