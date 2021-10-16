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
package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.internal;

import java.util.logging.Logger;

import org.eclipse.sensinact.gateway.core.filtering.Filtering;
import org.eclipse.sensinact.gateway.core.filtering.FilteringType;
import org.osgi.service.component.annotations.Component;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * {@link Filtering} implementation allowing to apply a JsonPath expression to
 * the result object to be filtered
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@FilteringType(JsonPathFiltering.JSONPATH)
@Component(immediate=true, service = Filtering.class)
public class JsonPathFiltering implements Filtering {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
	
	protected static final Logger LOG = Logger.getLogger(JsonPathFiltering.class.getName());

	public static final String JSONPATH="jsonpath";
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//


    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        return JSONPATH.equals(type);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#apply(java.lang.String, java.lang.Object)
     */
    @Override
    public String apply(String definition, Object result) {
        if (definition == null) {
            return String.valueOf(result);
        }
        try {
            DocumentContext dc = JsonPath.parse(String.valueOf(result));
            Object object = dc.read(definition);
            if (object.getClass() == String.class) {
                return new StringBuilder().append("\"").append(object).append("\"").toString();
            } else {
                return String.valueOf(object);
            }
        } catch (Exception e) {
        	LOG.warning("Failed to process JsonPath");
            throw e;
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#getLDAPComponent()
     */
    @Override
    public String getLDAPComponent(String definition) {
        return null;
    }
}
