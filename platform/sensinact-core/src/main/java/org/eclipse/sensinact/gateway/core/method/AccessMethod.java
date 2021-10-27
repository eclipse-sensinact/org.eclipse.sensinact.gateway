/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.method;

//import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;
//import org.eclipse.sensinact.gateway.common.primitive.Typable;
//import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.ModelElementProxy;

/**
 * A method provided by an {@link ModelElementProxy}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethod<T, R extends AccessMethodResponse<T>> extends Nameable, Describable, PathElement {
	public static final String REQUEST_ID_KEY = "rid";

	public static final String GET = "GET".intern();
	public static final String SET = "SET".intern();
	public static final String ACT = "ACT".intern();
	public static final String SUBSCRIBE = "SUBSCRIBE".intern();
	public static final String UNSUBSCRIBE = "UNSUBSCRIBE".intern();
	public static final String DESCRIBE = "DESCRIBE".intern();

	public static class Type {
		private static final Map<String, Type> METHODS = new HashMap<String, Type>();
		private static boolean initialized = false;

		static {
			initialize();
		}

		/**
		 * 
		 */
		private static final void initialize() {
			if (initialized) 
				return;
			initialized = true;
			new Type(AccessMethod.GET, AccessMethodResponse.Response.GET_RESPONSE);
			new Type(AccessMethod.SET, AccessMethodResponse.Response.SET_RESPONSE);
			new Type(AccessMethod.ACT, AccessMethodResponse.Response.ACT_RESPONSE);
			new Type(AccessMethod.SUBSCRIBE, AccessMethodResponse.Response.SUBSCRIBE_RESPONSE);
			new Type(AccessMethod.UNSUBSCRIBE, AccessMethodResponse.Response.UNSUBSCRIBE_RESPONSE);
			new Type(AccessMethod.DESCRIBE, AccessMethodResponse.Response.DESCRIBE_RESPONSE);
		}

		/**
		 * @param name
		 * @return
		 */
		public static final Type valueOf(String name) {
			if (!initialized) 
				initialize();
			if (name == null) 
				return null;
			return METHODS.get(name);
		}

		/**
		 * @param response
		 * @return
		 */
		public static final Type valueOf(AccessMethodResponse.Response response) {
			if (!initialized)
				initialize();
			if (response == null) 
				return null;
			Collection<Type> methods = METHODS.values();
			Iterator<Type> it = methods.iterator();
			Type m = null;
			for (;;) {
				if (!it.hasNext())
					break;
				m = it.next();
				if (response.equals(m.response))
					return m;
			}
			return null;
		}

		/**
		 * @return
		 */
		public static final Type[] values() {
			if (!initialized)
				initialize();
			Collection<Type> collection = METHODS.values();
			Type[] types = new Type[collection.size()];
			Iterator<Type> iterator = collection.iterator();
			while (iterator.hasNext()) {
				Type t = iterator.next();
				types[t.ordinal()] = t;
			}
			return types;
		}

		private final String name;
		private final AccessMethodResponse.Response response;
		private final int ordinal;

		/**
		 * @param name
		 * @param response
		 */
		public Type(String name, AccessMethodResponse.Response response) {
			if (valueOf(name) != null) {
				throw new IllegalArgumentException("This method already exists");
			}
			this.name = name;
			this.response = response;
			this.ordinal = METHODS.size();
			METHODS.put(this.name, this);
		}

		/**
		 * @return
		 */
		public String name() {
			return this.name;
		}

		/**
		 * @return
		 */
		public int ordinal() {
			return this.ordinal;
		}

		/**
		 * @return
		 */
		public AccessMethodResponse.Response getReturnedType() {
			return this.response;
		}

		@Override	
		public int hashCode() {
			return this.ordinal();
		}
		
		@Override	
		public boolean equals(Object o) {
			if (AccessMethod.Type.class.isAssignableFrom(o.getClass())) {
				return ((AccessMethod.Type) o).ordinal() == this.ordinal();
			}
			return false;
		}
	}

	Object EMPTY = new Object();

	/**
	 * Returns the set of {@link Signature}s of this AccessMethod
	 * 
	 * @return this AccessMethod's {@link Signature}s
	 */
	Set<Signature> getSignatures();

	/**
	 * Returns the number of {@link Signature}s of this AccessMethod
	 * 
	 * @return the number of {@link Signature}s of this AccessMethod
	 */
	int size();

	/**
	 * Returns {@link ErrorHandler} used to handle an error if it occurs
	 * 
	 * @return the {@link ErrorHandler} handling errors
	 */
	ErrorHandler getErrorHandler();

	/**
	 * Returns the {@link AccessMethod.Type} of this AccessMethod
	 * 
	 * @return this AccessMethod's {@link AccessMethod.Type}
	 */
	AccessMethod.Type getType();

	/**
	 * Invokes this method by using the objects array argument to parameterize the
	 * call
	 * 
	 * @param parameters
	 *            the objects array parameterizing the call
	 * @return the {@link AccessMethodResponse} result of the invocation
	 */
	R invoke(Object[] parameters);

}
