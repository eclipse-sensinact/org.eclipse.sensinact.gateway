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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.tree.PathNodeFactory;
import org.eclipse.sensinact.gateway.util.tree.PathTree;

/**
 * {@link AccessTree} implementation whose nodes define access rights to 
 * an instance of the sensiNact's data model 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessTreeImpl<N extends AccessNodeImpl<N>> extends PathTree<N>
implements MutableAccessTree<AccessNodeImpl<N>>
{
	//********************************************************************//
	//						NESTED DECLARATIONS	     					  //
	//********************************************************************//
	
    /**
     * {@link PathNodeFactory} dedicated to {@link AccessNodeImpl} instantiation
     * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
     */
    private static class AccessNodeFactory<N extends AccessNodeImpl<N>>
    implements PathNodeFactory<N>
    {    	
    	private Mediator mediator;

		/**
		 * Constructor
		 * 
		 * @param mediator
		 * 		the {@link Mediator} allowing to interact
		 * 		with the OSGi host environment
		 */
		AccessNodeFactory(Mediator mediator)
    	{
    		this.mediator = mediator;
    	}
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.tree.PathNodeFactory#
		 * createPathNode(java.lang.String)
		 */
		public N createPathNode(String nodeName)
    	{
    		return (N) new AccessNodeImpl<N>(mediator, nodeName, false);
    	}

		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.tree.PathNodeFactory#
		 * createPatternNode(java.lang.String)
		 */
		public N createPatternNode(String nodeName)
    	{
    		return (N) new AccessNodeImpl<N>(mediator, nodeName, true);
    	}
		
		/**
		 * @inheritDoc
		 *
		 * @see org.eclipse.sensinact.gateway.util.tree.PathNodeFactory#
		 * createRootNode()
		 */
		public N createRootNode()
    	{
    		return (N) new AccessNodeImpl<N>(mediator);
    	}
    }
    
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
	 * The {@link Mediator} allowing to interact with the 
	 * OSGi host environment
	 */
	protected Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the AccessTree to be 
	 * instantiated to interact with the  OSGi host environment
	 */
	public AccessTreeImpl(Mediator mediator) 
	{
		super(new AccessNodeFactory<N>(mediator));
		this.mediator = mediator;
	}

	/**
	 * 
	 * @param option the {@link AccessProfileOption}  wrapping the 
	 * {@link AccessProfile} applying to the root node of this {@link 
	 * AccessTree}
	 * 
	 * @return this AccessTree instance
	 */
	public AccessTreeImpl<N> withAccessProfile(AccessProfileOption option)
	{
		super.root.withAccessProfile(option.getAccessProfile());
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.security.AccessTree#isMutable()
	 */
	@Override
	public boolean isMutable()
	{
		return true;
	}
}
