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
 * Signature of a service describing a frame ; A FrameModel is an "instance" 
 * of a FrameType 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface FrameModel extends FrameType
{			
	/**
	 * Add a child FrameModel to the current one
	 * 
	 * @param model
	 * 		the child FrameModel 
	 */
	 void addChild(FrameModel model);

	/**
	 * Return the children FrameModel enumeration
	 *  
	 * @return
	 * 		the children FrameModel enumeration
	 */
	FrameModel[] children();
		
	 /**
	 * Return the offset of the current frame in its parent one
	 *  
	 * @return
	 * 		the offset of the current frame
	 */
	 int offset();
		
	/**
	 * Define the offset of the current FrameModel implementation instance
	 */
	void setOffset(int offset);
	
	/**
	 * Return the number of sub-frames contained by the current 
	 * FrameModel
	 *  
	 * @return
	 * 		the number of sub-frames
	 */
	int size();
	
	/**
	 * Checks the validity of the model
	 * 
	 * @throws FrameModelException
	 */
	void checkValid() throws FrameModelException;
}
