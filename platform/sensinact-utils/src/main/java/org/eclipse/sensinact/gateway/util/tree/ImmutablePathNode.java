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
package org.eclipse.sensinact.gateway.util.tree;

import org.eclipse.sensinact.gateway.util.UriUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ImmutablePathNode<P extends ImmutablePathNode<P>> implements Iterable<P> {
    protected final P parent;
    protected final String nodeName;
    protected final ImmutablePathNodeList<P> children;
    protected final Pattern pattern;

    protected final boolean isRoot;
    protected final boolean isPattern;

    /**
     * Constructor
     *
     * @param nodeName the name of the PathNode
     *                 to be instantiated
     */
    public ImmutablePathNode(PathNodeList<?> children) {
        this(UriUtils.PATH_SEPARATOR, children);
    }

    /**
     * Constructor
     *
     * @param nodeName the name of the PathNode
     *                 to be instantiated
     */
    public ImmutablePathNode(String nodeName, PathNodeList<?> children) {
        this(null, nodeName, false, children);
    }

    /**
     * Constructor
     *
     * @param nodeName the name of the PathNode
     *                 to be instantiated
     */
    public ImmutablePathNode(P parent, String nodeName, boolean isPattern, PathNodeList<?> children) {
        this.parent = parent;
        this.nodeName = nodeName;
        this.isRoot = (nodeName.intern() == UriUtils.PATH_SEPARATOR);
        this.isPattern = isPattern;
        this.pattern = isPattern ? Pattern.compile(nodeName) : null;
        if (children != null) {
            this.children = children.immutable((P) this);
        } else {
            this.children = new ImmutablePathNodeList<P>(Collections.<ImmutablePathNodeBucket<P>>emptyList());
        }
    }

    /**
     * @param path
     * @return
     */
    public P get(String path) {
        return this.get(UriUtils.getUriElements(path), 0);
    }

    /**
     * @param path
     * @param index
     * @return
     */
    public P get(String[] path, int index) {
        P node = null;

        if ((isRoot && index != 0) || (!isRoot && (path.length - index < 1 || !this.equals(path[index])))) {
            return node;
        }
        int inc = isRoot ? 0 : 1;
        node = (P) this;
        P child = null;

        if (path.length - index > inc && (child = this.children.get(path[index + inc])) != null) {
            node = child.get(path, index + inc);
        }
        return node;
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return nodeName.hashCode();
    }

    /**
     * Returns the name of this PathNode
     *
     * @return this PathNode's name
     */
    public String getName() {
        return this.nodeName;
    }

    /**
     * Returns the path of this PathNode
     *
     * @return this PathNode's path
     */
    public String getPath() {
        if (this.parent == null) {
            return "";
        }
        return UriUtils.getUri(new String[]{this.parent.getPath(), this.nodeName});
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        String objectName = null;

        if (ImmutablePathNode.class.isAssignableFrom(object.getClass())) {
            objectName = ((ImmutablePathNode<?>) object).nodeName;

        } else if (String.class == object.getClass()) {
            objectName = (String) object;
        }
        if (objectName == null) {
            return false;
        }
        return !isPattern ? this.nodeName.equals(objectName) : pattern.matcher(objectName).matches();
    }

    /**
     * Returns the size of this node, meaning the
     * number of its children
     *
     * @return the number of this node's children
     */
    public int size() {
        return this.children.size();
    }

    /**
     * @inheritDoc
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.nodeName);
        builder.append("[");
        Iterator<P> iterator = children.iterator();
        while (iterator.hasNext()) {
            P node = iterator.next();
            builder.append(node.toString());
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * @inheritDoc
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<P> iterator() {
        return this.children.iterator();
    }
}