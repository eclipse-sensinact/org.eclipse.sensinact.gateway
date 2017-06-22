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


import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Combination of constraints tool
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Expression extends LinkedList<Constraint> 
implements Constraint
{
	static enum LogicalOperator
	{
		AND,
		OR;
	}

	private final LogicalOperator operator;
	
	/**
	 * Constructor
	 * 
	 * @param operator
	 * 		the {@link LogicalOperator} of the
	 * 		{@link Constraint} Expression to instantiate
	 */
	public Expression(LogicalOperator operator)
	{
		this.operator = operator;
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see Constraint#getOperator()
	 */
	public String getOperator()
	{
		return this.operator.name();
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Constraint#complies(java.lang.Object)
	 */
	public boolean complies(Object value)
	{
		Iterator<Constraint> iterator = super.iterator();
		while(iterator.hasNext())
		{
			Constraint constraint = iterator.next();
			boolean complies = constraint.complies(value);
			
			if(!complies && this.operator.equals(
					LogicalOperator.AND))
			{
				return false;
				
			} else if(complies && this.operator.equals(
					LogicalOperator.OR))
			{
				break;
			}
		}
		return !this.operator.equals(
				LogicalOperator.OR);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Constraint#getComplement()
	 */
	public Constraint getComplement()
	{
		Expression complement = new Expression(
				this.operator.equals(LogicalOperator.AND)
				?LogicalOperator.OR:LogicalOperator.AND);
		
		Iterator<Constraint> iterator = super.iterator();
		while(iterator.hasNext())
		{
			Constraint constraint = iterator.next();
			complement.add(constraint.getComplement());
		}
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
		int index = 0;		
		StringBuilder builder = new StringBuilder();
		builder.append(JSONUtils.OPEN_BRACKET);
		builder.append(JSONUtils.QUOTE);
		builder.append(this.operator.name());
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COMMA);
		
		Iterator<Constraint> iterator = super.iterator();
		while(iterator.hasNext())
		{
			builder.append(index > 0?JSONUtils.COMMA:JSONUtils.EMPTY);
			builder.append(iterator.next().getJSON());	
			index++;
		}
		builder.append(JSONUtils.CLOSE_BRACKET);
	    return builder.toString();
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Constraint#isComplement()
	 */
	@Override
	public boolean isComplement() 
	{
		return false;
	}
}
