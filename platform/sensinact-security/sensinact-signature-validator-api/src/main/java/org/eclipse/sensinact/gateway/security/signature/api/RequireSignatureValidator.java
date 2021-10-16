package org.eclipse.sensinact.gateway.security.signature.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.implementation.ImplementationNamespace;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.PACKAGE })
@Requirement(namespace = ImplementationNamespace.IMPLEMENTATION_NAMESPACE, //
		name = SignatureValidatorConstants.SIGNATURE_VALIDATOR_IMPLEMENTATION, //
		version = SignatureValidatorConstants.SIGNATURE_VALIDATOR_SPECIFICATION_VERSION)
public @interface RequireSignatureValidator {
	// This is a marker annotation.
}