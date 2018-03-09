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
package org.eclipse.sensinact.gateway.app.manager.component.data;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.Metadata;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * This class acts as a proxy to a sNa resource
 *
 * @author Rémi Druilhe
 */
public class ResourceData implements DataItf {

    private final Session session;
    private final String uri;

    public ResourceData(Session session, String uri) {
        this.session = session;
        this.uri = uri;
    }

    /**
     * Get the source sensiNact URI of this resource
     * @return the URI of the resource
     */
    public String getSourceUri() {
        return uri;
    }

    /**
     * The resource registered in the OSGi registry.
     * @return the resource. Null if the resource does not exist
     */
    public Resource getResource()
    {
    	String[] uriElements = UriUtils.getUriElements(getSourceUri());
    	if(uriElements.length != 3)
    	{
    		return null;
    	}
    	return session.resource(uriElements[0],uriElements[1],uriElements[2]);
    }
    
    /**
     * Get the value of the {@link Resource}
     * @return the value
     */
    public Object getValue() 
    {
        return getResource().get(DataResource.VALUE
        	).getResponse(DataResource.VALUE);
    }

    /**
     * Get the Java type of the {@link Resource}
     * @return the Java type
     */
    public Class<?> getType() 
    {
        return CastUtils.jsonTypeToJavaType((String) getResource(
        ).get(DataResource.VALUE).getResponse(DataResource.TYPE));
    }

    /**
     * Get the timestamp of the data
     * @return the timestamp of the data
     */
    public long getTimestamp()
    {
        return getResource().get(DataResource.VALUE
        	).getResponse(Long.class,Metadata.TIMESTAMP);
    }
}
