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

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.logging.Logger;

import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Constraint applying on set of data
 * 
 * @param <T>
 * 		the type of the handled data set
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ConstraintOnCollection<T> implements Constraint
{
	protected static final Logger LOGGER = Logger.getLogger(
			ConstraintOnCollection.class.getCanonicalName());
	
	/**
	 * String operator of this Constraint
	 */
	protected final String operator;

	/**
	 * Defines whether this constraint is the 
	 * logical complement of the raw defined constraint
	 */
	protected final boolean complement;
	
	/**
	 * Operand on which to base the constraint compliance evaluation
	 */
	protected final T[] operand;

	/**
	 * Operand array component type
	 */
	protected Class<T> operandClass;

	/**
	 * Mediator used to interact with the OSGi host
	 * environment 
	 */
	protected final ClassLoader classloader;

	/**
	 * @param operator
	 * @param operand
	 * @throws InvalidConstraintDefinitionException
	 */
	public ConstraintOnCollection(ClassLoader classloader, 
			String operator, T[] operand, 
			boolean complement)
	        throws InvalidConstraintDefinitionException
	{
		this.classloader = classloader;
		this.operator = operator;
		if (operand == null)
		{
			throw new InvalidConstraintDefinitionException(
			        "operand argument is required");
		}
		this.operand = operand;
		this.operandClass = (Class<T>) 
				this.operand.getClass().getComponentType();
		this.complement = complement;
	}

	/**
	 * Constructor
	 * 
	 * @param operator
	 *            String operator of this constraint
	 * @param operandClass
	 *            use the Enum class if the constraint applies on its constants
	 *            set use the component type or the array class if the
	 *            constraint applies on an array or a collection
	 * @param operand
	 *            use null if this constraint applies on the constants set of an
	 *            Enum class use the array or collection object if the
	 *            constraint applies on it
	 * @throws InvalidConstraintDefinitionException
	 */
	@SuppressWarnings("unchecked")
	public ConstraintOnCollection(ClassLoader classloader,
			String operator, Class<T> operandClass,
	        Object operand, boolean complement) 
	        throws InvalidConstraintDefinitionException
	{
		if (operandClass == null)
		{
			throw new InvalidConstraintDefinitionException(
					"type is required");
		}
		this.classloader = classloader;
		this.operator = operator;
		this.complement = complement;
		
		if (operand == null)
		{
			if (operandClass.isEnum())
			{
				this.operandClass = operandClass;
				this.operand = (T[]) ((Class<? extends Enum<?>>) 
						operandClass).getEnumConstants();
				return;
			} 
			throw new InvalidConstraintDefinitionException(
			        "If the operand argument is null the type "
			        + "must be an Enum one");
		}
		if (operandClass.isArray())
		{
			this.operandClass = (Class<T>) operandClass.getComponentType();

		} else if (Collection.class.isAssignableFrom(operandClass))
		{
			Type[] interfaces = operandClass.getGenericInterfaces();
			Class<?> generic = null;
			for (Type genericInterface : interfaces)
			{
				try
				{
					ParameterizedType parameterized = (ParameterizedType) genericInterface;
					if (Collection.class.isAssignableFrom(((Class<?>) 
							parameterized.getRawType())))
					{
						generic = (Class<T>) parameterized.getActualTypeArguments()[0];
						break;
					}
				} catch (ClassCastException e)
				{
					continue;
				}
			}
			if (generic == null)
			{
				throw new InvalidConstraintDefinitionException(
				"Unable to retrieve the type from the specified extended Collection one");
			}
			this.operandClass = (Class<T>) generic;

		} else
		{
			this.operandClass = operandClass;
		}
		Class<T> castedOperandClass = (Class<T>) 
				CastUtils.primitiveToComparable(
				this.operandClass);
		this.operandClass = castedOperandClass!=null
				?castedOperandClass:this.operandClass;		
		try		
		{
			this.operand = (T[]) CastUtils.castArray(
				this.classloader, ((T[]) Array.newInstance(
					this.operandClass, 0)).getClass(),
				operand);

		} catch (ClassCastException e)
		{
			throw new InvalidConstraintDefinitionException(e);
		}
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Constraint#getOperator()
	 */
	@Override
	public String getOperator()
	{
		return this.operator;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Constraint#isComplement()
	 */
	public boolean isComplement()
	{
		return this.complement;
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
		builder.append(TYPE_KEY);
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COLON);
		builder.append(JSONUtils.QUOTE);
		builder.append(CastUtils.writeClass(this.operandClass));
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COMMA);
		builder.append(JSONUtils.QUOTE);
		builder.append(COMPLEMENT_KEY);
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COLON);
		builder.append(this.complement);
		builder.append(JSONUtils.COMMA);
		builder.append(JSONUtils.QUOTE);
		builder.append(OPERAND_KEY);
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COLON);
		builder.append(JSONUtils.toJSONFormat(this.operand));
		builder.append(JSONUtils.CLOSE_BRACE);
	    return builder.toString();
    }
}
