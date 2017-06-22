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

import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Constraint applying on a value which is required to has changed
 * since a previous value
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Changed extends ConstraintOnComparable<Boolean>
{
	public static final String OPERATOR = "changed";
	
	/**
	 * @param complement
	 */
	public Changed(ClassLoader classloader)
	{
		this(classloader, false);
	}
	
	/**
	 * @param complement
	 */
	public Changed(ClassLoader classloader, boolean complement)
	{
		super(classloader, OPERATOR, new Boolean(true), complement);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see ConstraintOnComparable#doComplies(java.lang.Object)
	 */
	@Override
	protected boolean doComplies(Boolean castedValue)
	{
        return castedValue.booleanValue()||isComplement();
	}	

	/**
	 * @inheritDoc
	 *
	 * @see Constraint#getComplement()
	 */
    @Override
    public Constraint getComplement()
    {
    	Changed complement = new Changed(super.classloader, !this.complement);			
		return complement;
    }


	/**
	 * @inheritDoc
	 *
	 * @see JSONable#getJSON()
	 */
	@Override
    public String getJSON()
    {
		StringBuilder builder = new StringBuilder();
		builder.append(JSONUtils.OPEN_BRACE);
		builder.append(JSONUtils.QUOTE);
		builder.append(OPERATOR_KEY);
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COLON);
		builder.append(JSONUtils.QUOTE);
		builder.append(this.getOperator());
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COMMA);
		builder.append(JSONUtils.QUOTE);
		builder.append(COMPLEMENT_KEY);
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COLON);
		builder.append(this.isComplement());
		builder.append(JSONUtils.CLOSE_BRACE);
	    return builder.toString();
    }
}
