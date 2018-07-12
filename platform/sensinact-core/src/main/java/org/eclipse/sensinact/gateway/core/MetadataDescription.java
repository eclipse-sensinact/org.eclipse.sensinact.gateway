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
import org.eclipse.sensinact.gateway.common.primitive.Modifiable;
import org.eclipse.sensinact.gateway.common.primitive.PrimitiveDescription;
import org.json.JSONObject;

/**
 * {@link Description} of a {@link Metadata}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MetadataDescription extends PrimitiveDescription {
    private static final String EMPTY = "";

    /**
     * Constructor
     *
     * @param metadata the {@link Metadata} for which to instantiate
     *                 the MetadataDescription
     */
    public MetadataDescription(Metadata metadata) {
        super(metadata);
    }

    /**
     * @inheritDoc
     * @see Description #getJSONDescription()
     */
    @Override
    public String getJSONDescription() {
        JSONObject description = this.getJSONObjectDescription();
        if (description == null) {
            return EMPTY;
        }
        return description.toString();
    }

    /**
     * Returns the JSON object representation of the
     * described {@link Metadata}
     *
     * @return the JSON object representation of the
     * described {@link Metadata}
     */
    protected JSONObject getJSONObjectDescription() {
        if (Modifiable.FIXED.equals(super.modifiable)) {
            JSONObject description = super.getJSONObject();
            description.put(VALUE_KEY, PrimitiveDescription.toJson(this.getType(), this.getValue()));
            return description;
        }
        return null;
    }
}
