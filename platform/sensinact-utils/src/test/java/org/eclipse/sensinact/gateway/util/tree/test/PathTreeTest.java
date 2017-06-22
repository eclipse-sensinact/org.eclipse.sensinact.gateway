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
package org.eclipse.sensinact.gateway.util.tree.test;

import org.junit.Test;

import org.eclipse.sensinact.gateway.util.tree.PathTree;
import org.eclipse.sensinact.gateway.util.tree.PathNode;
import org.eclipse.sensinact.gateway.util.tree.PathNodeFactory;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class PathTreeTest
{
	
	class MyTree extends PathTree<PathNode>
	{
		
		MyTree()
		{
			super(new PathNodeFactory<PathNode>()
			{
				@Override
				public PathNode createPathNode(String nodeName)
				{
					return new PathNode(nodeName);
				}

				@Override
				public PathNode createPatternNode(String nodeName)
				{
					return new PathNode(nodeName,true);
				}

				@Override
				public PathNode createRootNode()
				{
					return new PathNode();
				}
				
			});
		}
	}
	
	private MyTree tree = new MyTree();
	
	/**
	 * 
	 */
	@Test
	public void testTree()
	{
		tree.add("/aaaaaaa");
		tree.add("/vvvvvvv");
		tree.add("/ccccccc");
		tree.add("/uuuuuuu");
		tree.add("/hhhhhhh");
		
		tree.add("/test/service/resource");
		tree.add("/test/", ".*", true);
		tree.add("/test/.*/location");	
		
		System.out.println(tree.toString());
		
		PathNode node = tree.get("/test/myservice/location");
		System.out.println(node.getPath());

		tree.delete("/test/service");
		System.out.println(tree.toString());
		
		tree.delete("/xxxxx");
		System.out.println(tree.toString());
		
		
	}
}
