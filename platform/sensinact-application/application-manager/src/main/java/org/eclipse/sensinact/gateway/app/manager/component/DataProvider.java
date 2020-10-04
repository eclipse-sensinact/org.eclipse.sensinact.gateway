/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.component;

public class DataProvider extends AbstractDataProvider {
    private final Class dataType;

    /**
     * Constructor
     *
     * @param uri the URI of the {@link DataProvider}
     */
    public DataProvider(String uri, Class dataType) {
        super(uri);
        this.dataType = dataType;
    }

    /**
     * Get the {@link Class} of the data
     *
     * @return the type of the data
     */
    public Class getDataType() {
        return dataType;
    }
}
