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
package org.eclipse.sensinact.gateway.common.automata;

/**
 * Signature of a service allowing to manipulate a frame
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface Frame
{	
	public static final int START_DELIMITED = 0;
	public static final int END_DELIMITED = 1;
	public static final int START_END_DELIMITED = 2;
	public static final int SIZE_DELIMITED = 3;
	public static final int SIZE_START_DELIMITED = 4;
	public static final int SIZE_END_DELIMITED = 5;
	public static final int SIZE_START_END_DELIMITED = 6;
	public static final int EMPTY_BUFFER_DELIMITED = 7;
	
	/**
	 * Returns this frame's bytes array
	 * 
	 * @return
	 * 		 this frame's bytes array
	 */
	byte[] getBytes();	
	
	/**
	 * Appends the byte passed as parameter to
	 * this frame
	 * 
	 * @param bte
	 * 		the byte to append
	 * 
	 * @throws FrameException 
	 */
	void append(byte bte) throws FrameException;
	
	/**
	 * Defines the byte value at the defined position
	 * 
	 * @param bte
	 * 		the byte value to set
	 * @param pos
	 * 		the position
	 *  
	 * @throws FrameException 
	 */
	void set(byte bte, int pos) throws FrameException;
		
	/**
	 * Returns the length of this frame
	 * 
	 * @return
	 * 		the length of this frame 
	 */
	int getLength();
	
	/**
	 * Defines the length of this frame
	 * 
	 * @param length
	 * 		 the length of this frame
	 */
	void setLength(int length);
	
	/**
	 * Defines the offset of this frame in its
	 * parent's onec c
	 * 
	 * @param length
	 * 		the offset of this frame in its parent's 
	 * 		bytes array
	 */
	void setOffset(int offset);
	
	/**
	 * Returns the parent Frame implementation instances
	 * or null if it has not been defined
	 * 
	 * @return
	 * 		the parent Frame
	 */
	Frame getParent();

	/**
	 * Lets the parent Frame implementation instances
	 * 
	 * @param parent 
	 * 		the parent Frame
	 */
	void setParent(Frame  parent);
	
	/**
	 * Adds a Frame child to the current one
	 * 
	 * @param child
	 * 		the Frame child to add
	 */
	void addChild(Frame child);

	/**
	 * Returns the Frame implementation instances array
	 * contained by the current one
	 * 
	 * @return
	 * 		the array of Frame children
	 */
	Frame[] getChildren();	

	/**
	 * Returns the number of children this frame
	 * 
	 * @return
	 * 		the number of children
	 */
	int size();
	
	/**
	 * Returns true if the frame is valid ; False
	 * otherwise
	 * 
	 * @return 
	 * 		this frame validity
	 */
	boolean isComplete();

	/**
	 * Removes empty Frame children if the current one
	 * is complete
	 */
	void clean();
}
