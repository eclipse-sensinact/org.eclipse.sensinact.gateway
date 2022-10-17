/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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