/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.tree;

/**
 * {@link PathNode}s linker in a {@link PathNodeList}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
class PathNodeBucket<P extends PathNode<P>> {
    PathNodeBucket<P> next = null;
    P node;
    int hash;

    /**
     * Constructor
     *
     * @param node the {@link PathNode} wrapped by
     *             the PathNodeBucket to be instantiated
     */
    PathNodeBucket(P node) {
        this.node = node;
        this.hash = node.hashCode();
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (PathNodeBucket.class.isAssignableFrom(object.getClass())) {
            return node.equals(((PathNodeBucket<?>) object).node);
        }
        return node.equals(object);
    }

    /**
     * @param ic
     * @param parent
     * @return
     */
    public <N extends ImmutablePathNode<N>> ImmutablePathNodeBucket<N> immutable(Class<N> ic, N parent) {
        return new ImmutablePathNodeBucket<N>(node.<N>immutable(ic, parent), next != null ? next.immutable(ic, parent) : null);
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return node.nodeName;
    }

}