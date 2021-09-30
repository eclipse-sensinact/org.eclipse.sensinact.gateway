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