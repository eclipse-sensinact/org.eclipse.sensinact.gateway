/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.app.manager.component;

/**
 * @author Rémi Druilhe
 */
public class ResourceDataProvider extends AbstractDataProvider {

    public ResourceDataProvider(String uri) {
        super(uri);
    }

    /**
     * @see AbstractDataProvider#getDataType()
     */
    public Class getDataType() {
        return null;
    }
}
