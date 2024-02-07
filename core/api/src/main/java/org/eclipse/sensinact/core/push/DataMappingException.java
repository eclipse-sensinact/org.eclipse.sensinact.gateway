/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.push;

public class DataMappingException extends DataUpdateException {

    private static final long serialVersionUID = -1312692222440022024L;

    public DataMappingException(String modelPackageUri, String model, String provider, String service, String resource,
            Object originalDto, Throwable cause) {
        super(modelPackageUri, model, provider, service, resource, originalDto,
                "Unable to map DTO data into an update " + cause.getMessage(), cause);
    }
}
