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
package org.eclipse.sensinact.gateway.common.automata.test;


import org.eclipse.sensinact.gateway.common.automata.AbstractFrame;
import org.eclipse.sensinact.gateway.common.automata.Frame;
import org.eclipse.sensinact.gateway.common.automata.FrameException;


/**
 * Implementation of the {@link Frame} interface for a
 * frame defining a size
 */
public class OneByteSizeFrame extends AbstractFrame
{	
	
	public OneByteSizeFrame()
	{
		super();
	}
	
	/**
	 * Define the associated integer size 
	 * 
	 * @param delimiter
	 * 		the associated integer size
	 * 
	 * @throws FrameException
	 */
	public void setSize(int size) throws FrameException
	{
		append((byte) size);		
	}

	/**
	 * Return the associated integer size
	 * 
	 * @return
	 * 		the associated integer size
	 */
	public int getSize()
	{
		return (super.getBytes()[0] & MASK);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see sensinact.box.services.api.frame.AbstractFrame#addBytes(byte[])
	 */
	@Override
	public void append(byte bte) throws FrameException 
	{		
		super.append(bte);
		Frame parent = getParent();
		while(parent.getParent()!=null)
		{
			parent = parent.getParent();
		}		
		parent.setLength((bte & MASK));
	}
}
