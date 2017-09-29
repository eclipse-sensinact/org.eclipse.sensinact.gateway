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
package org.eclipse.sensinact.gateway.core.message;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
import org.eclipse.sensinact.gateway.common.primitive.Typable;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * An SnaMessage provide information about a {@link ServiceProvider}, 
 * a {@link Service}, or a {@link Resource} of sensiNact. It can be the 
 * response to an access method call, relative to the lifecycle of 
 * an entity of the system or an value update, or an error
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaMessage<S extends Enum<S> & SnaMessageSubType &  KeysCollection> 
extends Typable<S>, PathElement, JSONable
{	
	public static final String PERMANENT_KEYS_PROPERTY = "PERMANENT_KEYS";
	
	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey<?>[]
	{
		new TypedKey<Integer>(SnaConstants.ACCESS_LEVEL, int.class, true),  
		new TypedKey<Enum>(SnaConstants.TYPE_KEY, Enum.class, false),  
		new TypedKey<String>(SnaConstants.URI_KEY, String.class, false)
	};
	
	static enum Type
	{
		LIFECYCLE,
		UPDATE,
		RESPONSE,
		ERROR;
	}	
	
	/**
	 *
	 */
	static final class KeysBuilder
	{
		private Class<? extends SnaMessage> extended;

		/**
		 * @param extended
		 */
		public KeysBuilder(Class<? extends SnaMessage> extended)
		{
			this.extended = extended;
		}
		
		/**
		 * @return
		 */
		public TypedKey<?>[] keys()
		{
			TypedKey<?>[] keys = new TypedKey[0];
			
			LinkedList<Class<SnaMessage>> list = 
				ReflectUtils.getOrderedImplementedInterfaces(
					SnaMessage.class, extended);
			
			Iterator<Class<SnaMessage>> iterator = list.descendingIterator();
			while(iterator.hasNext())
			{
				TypedKey<?>[] tmpKeys = ReflectUtils.getConstantValue(
					iterator.next(), SnaMessage.PERMANENT_KEYS_PROPERTY);
				
				if(tmpKeys != null)
				{
					int length = keys.length;
					TypedKey<?>[] newKeys = new TypedKey[length+tmpKeys.length];
					System.arraycopy(keys, 0, newKeys, 0, length);
					System.arraycopy(tmpKeys, 0, newKeys, length, tmpKeys.length);
					keys = newKeys;
				}
			}
			return keys;
		}
	}
	
}
