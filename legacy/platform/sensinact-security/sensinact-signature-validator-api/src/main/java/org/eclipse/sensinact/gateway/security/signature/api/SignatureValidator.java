/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.security.signature.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Capability;
import org.osgi.namespace.implementation.ImplementationNamespace;
import org.osgi.service.component.annotations.ComponentPropertyType;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.PACKAGE })
@ComponentPropertyType
@Capability(namespace = ImplementationNamespace.IMPLEMENTATION_NAMESPACE, //
		name = SignatureValidatorConstants.SIGNATURE_VALIDATOR_IMPLEMENTATION, //
		version = SignatureValidatorConstants.SIGNATURE_VALIDATOR_SPECIFICATION_VERSION)
public @interface SignatureValidator {
	String PREFIX_ = SignatureValidatorConstants.PREFIX_;

	String type();
}