package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.security.signature.api.BundleValidation;
import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;

@Component
public class MyBundleValidation implements BundleValidation {
	@Override
	public String check(Bundle bundle) throws BundleValidationException {
		return "xxxxxxxxxxx00000000";
	}
}