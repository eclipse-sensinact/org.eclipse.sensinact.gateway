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

package org.eclipse.sensinact.gateway.common.props;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * A typed set of properties
 *
 * @param <T>
 * 		extended Enum type
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class TypedProperties<T extends Enum<T> & KeysCollection> 
implements JSONable
{
		protected static final String TYPE = "type";
		
		protected final Map<TypedKey<?>, Object> properties;
		
		protected final Mediator mediator;

		/**
		 * Constructor
		 * 
		 * @param source
		 *            the name of the {@link Primitive} throwing the event
		 * @param type
		 *            the type of this event
		 * @param timestamp
		 *            the timestamp (milliseconds) of the PrimitiveEvent
		 */
		public TypedProperties(Mediator mediator, T type)
		{
			this.mediator = mediator;
			this.properties = new HashMap<TypedKey<?>, Object>();
			TypedKey<?> key = type.key(TypedProperties.TYPE);
			if(key == null)
			{
				this.putValue(new TypedKey<T>(
					TypedProperties.TYPE, (Class<T>) 
					type.getClass(),false), type);
			} else
			{
				this.putValue(key, type);
			}
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
			Iterator<Map.Entry<TypedKey<?>,Object>> iterator = 
					this.properties.entrySet().iterator();
			
			builder.append(JSONUtils.OPEN_BRACE);
			int index = 0;
			
			while(iterator.hasNext())
			{
				Map.Entry<TypedKey<?>,Object> entry = iterator.next();
				TypedKey<?> typedKey = entry.getKey();
				
				if(typedKey.isHidden())
				{
					continue;
				}		
				builder.append(index > 0?JSONUtils.COMMA:"");
				builder.append(JSONUtils.QUOTE);
				builder.append(typedKey.getName());
				builder.append(JSONUtils.QUOTE);
				builder.append(JSONUtils.COLON);
				builder.append(JSONUtils.toJSONFormat(entry.getValue()));
				index++;
			}
			builder.append(JSONUtils.CLOSE_BRACE);
			return builder.toString();
		}
		
		/**
		 * Associates the key - value pair passed as parameter to this
		 * PrimitiveEvent. For the predefined keys, the value is set
		 * only if it has not been already defined
		 * 
		 * @param key
		 *            the key of the property to set
		 * @param value
		 *            the value to set
		 */
		protected void putValue(TypedKey<?> key, Object value)
		{			
			this.properties.put(key, value);
		}
		
		/**
		 * Associates the key - value pair passed as parameter to this
		 * PrimitiveEvent. For the predefined keys, the value is set
		 * only if it has not been already defined
		 * 
		 * @param key
		 *            the key of the property to set
		 * @param value
		 *            the value to set
		 */
		protected void putValue(String key, Object value)
		{	
			this.putValue(key,value, false);
		}

		/**
		 * Associates the key - value pair passed as parameter to this
		 * PrimitiveEvent. For the predefined keys, the value is set
		 * only if it has not been already defined
		 * 
		 * @param key the key of the property to set
		 * @param value the value to set
		 * @param hidden the hidden status of the TypedKey to be
		 * created if relevant
		 */
		protected void putValue(String key, Object value, boolean hidden)
		{	
			TypedKey<?> typedKey = this.getType().key(key);
			if(typedKey == null)
			{
				typedKey = new TypedKey<Object>(key, Object.class, hidden);
			}
			this.putValue(typedKey,value);
		}

		/**
		 * Associates the key - value pair passed as parameter to this
		 * PrimitiveEvent. For the predefined keys, the value is set
		 * only if it has not been already defined
		 * 
		 * @param key
		 *            the key of the property to set
		 * @param value
		 *            the value to set
		 */
		public void put(String key, Object value)
		{	
			this.put(key, value, false);
		}
		
		/**
		 * Associates the key - value pair passed as parameter to this
		 * PrimitiveEvent. For the predefined keys, the value is set
		 * only if it has not been already defined
		 * 
		 * @param key the key of the property to set
		 * @param value the value to set
		 * @param hidden the hidden status of the TypedKey to be
		 * created if relevant
		 */
		public void put(String key, Object value, boolean hidden)
		{			
			if (!this.getType().keys().contains(
					new Name<TypedKey<?>>(key)))
			{
				this.putValue(key, value, hidden);
			}
		}
		
		/**
		 * Removes from the key (and its associated value) from
		 * this TypedProperties 
		 * 
		 * @param key
		 *        the key of the property to remove
		 */
		public Object remove(String key)
		{			
			if (!this.getType().keys().contains(
					new Name<TypedKey<?>>(key)))
			{
				return this.properties.remove(key);
			}
			return null;
		}
		
		/**
		 * Returns the value object of the property whose
		 * name is passed as parameter
		 * 
		 * @return
		 * 		 the value object for the specified 
		 * 		property
		 */
		@SuppressWarnings("unchecked")
		public <V> V get(String key)
		{
			TypedKey<V> typedKey = null;
			
			Object object = this.properties.get(
			    new Name<TypedKey<?>>(key));				
			try
			{
				typedKey = (TypedKey<V>) 
						this.getType().key(key);
				
			} finally
			{
				if(object == null)
				{
					return (V) object;
				}
				if(typedKey != null && 
					!typedKey.getType().isAssignableFrom(
							object.getClass()))
				{
					return CastUtils.cast(this.mediator.getClassLoader(), 
						typedKey.getType(),object);
				}
			}			
			return (V)object;
		}

		/**
		 * Returns this TypedProperties type
		 * 
		 * @return this TypedProperties type
		 */
		public T getType()
		{
			return (T) this.properties.get(new Name<TypedKey>(
					TypedProperties.TYPE));
		}
}
