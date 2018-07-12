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
/**
 *
 */
package org.eclipse.sensinact.gateway.core.security;

import org.eclipse.sensinact.gateway.util.tree.PathTree;

/**
 * Extended {@link PathTree} gathering access rights to a sensiNact
 * gateway's data model instance using an hierarchical data structure
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessTree<A extends AccessNode> {
    //********************************************************************//
    //						NESTED DECLARATIONS	     					  //
    //********************************************************************//

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//

    //********************************************************************//
    //						STATIC DECLARATIONS  						  //
    //********************************************************************//

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    /**
     * Returns the root {@link AccessNode} of this AccessTree
     *
     * @return this AccessTree's root node
     */
    A getRoot();

    /**
     * Returns true if this AccessTree is mutable, meaning that
     * it is possible to add/remove {@link AccessNode} to/from it
     *
     * @return <ul>
     * <li>true if this AccessTree is mutable</li>
     * <li>false otherwise</li>
     * </ul>
     */
    boolean isMutable();
}
