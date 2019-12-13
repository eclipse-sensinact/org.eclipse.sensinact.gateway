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
package org.eclipse.sensinact.gateway.core.remote;

public interface SensinactCoreBaseIface {

    public String namespace();
    public String getAll(String identifier, final String filter);
    public String getProviders(String identifier, String filter);
    public String getProvider(String identifier, final String serviceProviderId);
    public String getServices(String identifier, final String serviceProviderId);
    public String getService(String identifier, final String serviceProviderId,final String serviceId);
    public String getResources(String identifier, final String serviceProviderId, final String serviceId);
    public String getResource(String identifier, final String serviceProviderId, final String serviceId,final String resourceId);
    public String get(String identifier, final String serviceProviderId, final String serviceId,
                          final String resourceId, final String attributeId);
    public String set(String identifier,final String serviceProviderId, final String serviceId,
                      final String resourceId, final String attributeId, final String parameter);
    public String act(String identifier, final String serviceProviderId, final String serviceId,
                      final String resourceId, final String parameters);

}
