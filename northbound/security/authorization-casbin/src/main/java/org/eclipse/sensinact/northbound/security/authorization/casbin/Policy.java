/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/

package org.eclipse.sensinact.northbound.security.authorization.casbin;

import java.util.List;
import java.util.Optional;

import org.casbin.jcasbin.main.Enforcer;

public record Policy(String subject, String modelPackageUri, String model, String provider, String service,
        String resource, String level, PolicyEffect eft, int priority) {

    /**
     * Converts this policy to a a list of strings to give to
     * {@link Enforcer#addPolicy(List)}
     *
     * @return A list of strings
     */
    public List<String> toList() {
        return List.of(Optional.ofNullable(subject).map(String::trim).orElse("*"), normalize(modelPackageUri),
                normalize(model), normalize(provider), normalize(service), normalize(resource), normalize(level),
                Optional.ofNullable(eft).map(PolicyEffect::name).orElse(PolicyEffect.deny.name()),
                Integer.toString(priority));
    }

    /**
     * Ensures that the pattern used in the policy is correct.
     *
     * @param str Input string
     * @return Normalized string
     */
    private String normalize(final String str) {
        if (str == null || str.isBlank() || str.equalsIgnoreCase("*")) {
            return ".*";
        }

        return str.trim();
    }
}
