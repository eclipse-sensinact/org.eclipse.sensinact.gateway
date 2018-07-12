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
package org.eclipse.sensinact.gateway.nthbnd.jsonpath;

import com.jayway.jsonpath.Defaults;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.builder.NodeBuilder;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.builder.JsonOrgNodeBuilder;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.json.JsonOrgJsonProvider;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.mapper.JsonOrgMappingProvider;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonOrgDefaults implements Defaults {
    private final MappingProvider mappingProvider = new JsonOrgMappingProvider();

    public JsonOrgDefaults() {
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.Defaults#jsonProvider()
     */
    @Override
    public JsonProvider jsonProvider() {
        return new JsonOrgJsonProvider();
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.Defaults#options()
     */
    @Override
    public Set<Option> options() {
        return EnumSet.noneOf(Option.class);
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.Defaults#mappingProvider()
     */
    @Override
    public MappingProvider mappingProvider() {
        return mappingProvider;
    }

    /**
     * @inheritDoc
     * @see com.jayway.jsonpath.Defaults#nodeBuilder()
     */
    @Override
    public NodeBuilder nodeBuilder() {
        return new JsonOrgNodeBuilder();
    }
}
