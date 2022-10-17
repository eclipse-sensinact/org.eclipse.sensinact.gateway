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

import org.eclipse.sensinact.gateway.security.signature.exception.BundleValidationException;
import org.osgi.framework.Bundle;

/**
 * BundleValidation defines a service for checking the
 * validity of cryptographic signature of a Bundle.
 */
public interface BundleValidation {
    String SHA1_DIGEST_TYPE = "SHA1-Digest";
    String SHA1_DIGEST_MF_MAIN = "SHA1-Digest-Manifest-Main-Attributes";

    /**
     * Check whether the {@link Bundle} whose {@link BundleContext}
     * is passed as parameter is valid, and returns its manifest
     * file signature if it is the case; otherwise returns null
     *
     * @param bundle the OSGi {@link BundleContext} of the {@link
     *               Bundle} to validate
     * @return the bundle's manifest signature if valid;
     * null otherwise
     * @throws BundleValidationException
     */
    String check(Bundle bundle) throws BundleValidationException;
}
