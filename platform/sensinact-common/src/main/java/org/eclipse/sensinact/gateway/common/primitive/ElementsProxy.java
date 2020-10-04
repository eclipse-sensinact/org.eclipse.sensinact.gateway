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
package org.eclipse.sensinact.gateway.common.primitive;

import java.util.Enumeration;

/**
 * An Elements proxy holds its element proxies
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ElementsProxy<P extends Nameable> extends Describable, PathElement {
    /**
     * Returns the &lt;P&gt; typed element held by this
     * ElementsProxy and whose name is passed as parameter.
     *
     * @param name the name of the searched &lt;P&gt; typed element
     * @return the specified  &lt;P&gt; typed element held by this
     * ElementsProxy
     */
    P element(String name);

    /**
     * Returns the {@link Enumeration} of the &lt;P&gt; typed elements
     * held by this ElementsProxy
     *
     * @return this ElementsProxy's elements {@link Enumeration}
     */
    Enumeration<P> elements();

    /**
     * @param name
     * @return
     */
    P removeElement(String name);

    /**
     * @param element
     * @return
     */
    boolean addElement(P element);

    /**
     * Returns true if this ElementsProxy is one of an accessible
     * {@link Elements} ; otherwise returns false
     *
     * @return <ul><li>true if the {@link Elements} proxied by
     * this ElementsProxy is accessible</li>
     * <li>false otherwise</li>
     * </ul>
     */
    boolean isAccessible();
}
