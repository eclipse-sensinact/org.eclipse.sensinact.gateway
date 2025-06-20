/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.rest.impl;

public class BundleDTO {
    /**
     * Symbolic name of the bundle
     */
    public String name;
    /**
     * Bundle version
     */
    public String version;
    /**
     * Git tag or branch name
     */
    public String git_ref;
    /**
     * Git hash
     */
    public String git_sha;
}
