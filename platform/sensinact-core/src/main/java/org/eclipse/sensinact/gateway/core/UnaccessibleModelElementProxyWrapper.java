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

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;

import java.util.Collections;
import java.util.Enumeration;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnaccessibleModelElementProxyWrapper extends ElementsProxyWrapper<UnaccessibleModelElementProxy, Nameable> {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * @param modelElement
     * @param proxy
     * @param tree
     */
    protected UnaccessibleModelElementProxyWrapper(UnaccessibleModelElementProxy proxy) {
        super(proxy);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#element(java.lang.String)
     */
    @Override
    public Nameable element(String name) {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#elements()
     */
    @Override
    public Enumeration<Nameable> elements() {
        return Collections.<Nameable>emptyEnumeration();
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.ElementsProxy#isAccessible()
     */
    @Override
    public boolean isAccessible() {
        return false;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.Describable#getDescription()
     */
    @Override
    public Description getDescription() {
        return new Description() {
            @Override
            public String getJSON() {
                return getJSONDescription();
            }

            @Override
            public String getName() {
                return proxy.getName();
            }

            @Override
            public String getJSONDescription() {
                StringBuilder builder = new StringBuilder();
                builder.append("{\"type\":\"DESCRIBE_ERROR\",\"statusCode\":");
                builder.append(SnaErrorfulMessage.FORBIDDEN_ERROR_CODE);
                builder.append(",\"errors\":[");
                builder.append("{\"message\":\"Unaccessible object\",\"trace\":[]}]}");
                return builder.toString();
            }
        };
    }
}
