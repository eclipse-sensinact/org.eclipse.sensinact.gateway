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
 * @author christophe
 *
 */
public class AccessTree extends PathTree<AccessNode>
{
	//********************************************************************//
	//						NESTED DECLARATIONS	     					  //
	//********************************************************************//
	
    private static class AccessNodeFactory implements PathNodeFactory<AccessNode>
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
    	 * @see PathNodeFactory#createPathNode(java.lang.String)
    	 */
    	public AccessNode createPathNode(String nodeName)
    	{
    		return new AccessNode(mediator, nodeName, false);
    	}

    	/**
    	 * @inheritDoc
    	 * 
    	 * @see PathNodeFactory#createPatternNode(java.lang.String)
    	 */
    	public AccessNode createPatternNode(String nodeName)
    	{
    		return new AccessNode(mediator, nodeName, true);
    	}

    	/**
    	 * @inheritDoc
    	 * 
    	 * @see PathNodeFactory#createRootNode()
    	 */
    	public AccessNode createRootNode()
    	{
    		return new AccessNode(mediator);
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

	private Mediator mediator;

	/**
	 * @param factory
	 */
	public AccessTree(Mediator mediator) 
	{
		super(new AccessNodeFactory(mediator));
		this.mediator = mediator;
	}

	/**
	 * @param allAnonymous
	 * @return
	 */
	public AccessTree withAccessProfile(AccessProfileOption option)
	{
		super.root.withAccessProfile(option.getAccessProfile());
		return this;
	}
}
