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
class ImmutablePathNodeBucket<P extends ImmutablePathNode<P>> {
    final ImmutablePathNodeBucket<P> next;
    final P node;
    final int hash;

    /**
     * Constructor
     *
     * @param node the {@link ImmutablePathNode} wrapped by
     *             the ImmutablePathNodeBucket to be instantiated
     * @param next
     */
    ImmutablePathNodeBucket(P node, ImmutablePathNodeBucket<P> next) {
        this.node = node;
        this.hash = node.hashCode();
        this.next = next;
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (ImmutablePathNodeBucket.class.isAssignableFrom(object.getClass())) {
            return node.equals(((ImmutablePathNodeBucket<?>) object).node);
        }
        return node.equals(object);
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return node.nodeName;
    }

}