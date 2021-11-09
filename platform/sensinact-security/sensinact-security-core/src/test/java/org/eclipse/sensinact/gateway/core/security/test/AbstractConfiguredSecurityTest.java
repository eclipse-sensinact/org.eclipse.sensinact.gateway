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