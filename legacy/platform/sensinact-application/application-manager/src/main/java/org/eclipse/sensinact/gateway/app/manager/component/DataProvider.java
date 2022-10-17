/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.component;

public class DataProvider extends AbstractDataProvider {
    private final Class<?> dataType;

    /**
     * Constructor
     *
     * @param uri the URI of the {@link DataProvider}
     */
    public DataProvider(String uri, Class<?> dataType) {
        super(uri);
        this.dataType = dataType;
    }

    /**
     * Get the {@link Class} of the data
     *
     * @return the type of the data
     */
    public Class<?> getDataType() {
        return dataType;
    }
}
