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
package org.eclipse.sensinact.gateway.core;

/**
 * A {@link ProcessableData} targeting one {@link ServiceProviderImpl}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ServiceProviderProcessableData<S extends ServiceProcessableData<?>> extends ServiceProcessableContainer<S> {
    /**
     * Returns the string identifier of a {@link ServiceProvider}
     * targeted by this SubPacket
     *
     * @return the identifier of the targeted {@link ServiceProvider}
     */
    public String getServiceProviderIdentifier();

}
