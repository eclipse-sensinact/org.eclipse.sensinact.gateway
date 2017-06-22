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

package org.eclipse.sensinact.gateway.common.constraint;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MaxLength extends ConstraintOnCollectionSize
{
	public static final String OPERATOR = "-";

	/**
	 * Constructor
	 * 
	 * @param operator
	 *            String operator of this constraint
	 * @param length
	 *            integer value on which will be based the compliance evaluation
	 *            of the size of a collection
	 */
	public MaxLength(ClassLoader classloader,
			int length, boolean complement)
	{
		super(classloader, OPERATOR, length, complement);
	}

	/**
	 * @inheritDoc
	 * 
	 * @see ConstraintOnCollectionSize#
	 * doComplies(int)
	 */
	@Override
	protected boolean doComplies(int length)
	{
		return this.length >= length;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Constraint#getComplement()
	 */
    @Override
    public Constraint getComplement()
    {
    	MaxLength complement = null;
		complement = new MaxLength(
				super.classloader,
				super.length,
				!this.complement);			
		return complement;
    }
}
