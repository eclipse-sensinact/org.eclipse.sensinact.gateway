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
package org.eclipse.sensinact.gateway.common.primitive;

/**
 * A Description service provides a complete JSON formated
 * description string of an other service
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Description extends JSONable, Nameable {
    /**
     * Returns the JSON formated description string of the
     * object described by this Description instance. Unlike
     * the inherited {@link #getJSON()} which is context
     * dependent the description returned by this method
     * should be "complete"
     *
     * @return the complete JSON formated description string
     * of the object described by this Description
     */
    String getJSONDescription();
}
