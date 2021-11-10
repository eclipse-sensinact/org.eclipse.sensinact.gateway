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
package org.eclipse.sensinact.gateway.common.props;

import java.util.Objects;

import org.eclipse.sensinact.gateway.common.primitive.Nameable;

/**
 * A string key mapped to a type
 *
 * @param <T> the type of the key
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class TypedKey<T> implements Nameable {
    private final String name;
    private final Class<T> type;
    private final boolean hidden;

    /**
     * @param name this key's name
     * @param type the associated type
     */
    public TypedKey(String name, Class<T> type, boolean hidden) {
        this.name = name;
        this.type = type;
        this.hidden = hidden;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.primitive.core.Nameable#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return
     */
    public Class<T> getType() {
        return this.type;
    }

    @Override
    public boolean equals(Object obj) {
    	if (this == obj)
    		return true;
    	if (obj == null)
    		return false;
    	if (getClass() != obj.getClass())
    		return false;
    	TypedKey<?> other = (TypedKey<?>) obj;
    	return Objects.equals(name, other.name);
    }

    @Override
	public int hashCode() {
		return Objects.hash(name);
	}


	/**
     * Returns false if this TypeKey has to appear
     * into the JSON formated string describing the
     * TypedProperties instance holding it; otherwise
     * returns true
     *
     * @return <ul>
     * <li>true if this TypeKey is hidden</li>
     * <li>false otherwise</li>
     * </ul>
     */
    public boolean isHidden() {
        return this.hidden;
    }
}
