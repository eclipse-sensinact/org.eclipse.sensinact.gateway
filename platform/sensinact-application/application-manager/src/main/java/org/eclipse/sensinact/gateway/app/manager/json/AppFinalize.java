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
package org.eclipse.sensinact.gateway.app.manager.json;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONObject;

/**
 * Finalize is not used for now
 *
 * @author Rémi Druilhe
 */
public class AppFinalize implements JSONable {
    public AppFinalize(JSONObject finalize) {
    }

    /**
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return new JSONObject().toString();
    }
}
