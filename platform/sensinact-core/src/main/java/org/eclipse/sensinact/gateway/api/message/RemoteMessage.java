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
package org.eclipse.sensinact.gateway.api.message;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;

/**
 * Lifecycle dedicated {@link SnaMessage}
 *
 * @author <a href="mailto:cmunilla@cmssi.fr">Christophe Munilla</a>
 */
public interface RemoteMessage extends NotificationMessage<RemoteMessage.Remote> {
	public static final SnaMessage.Type TYPE = SnaMessage.Type.REMOTE;

	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[] {
			new TypedKey<String>(SnaConstants.NAMESPACE, String.class, false),
			new TypedKey<Remote>(SnaConstants.REMOTE, Remote.class, false) };

	
	enum Remote implements MessageSubType, KeysCollection {
		CONNECTED, DISCONNECTED;

		final Set<TypedKey<?>> keys;

		Remote() {
			List<TypedKey<?>> list = Arrays.asList(new SnaMessage.KeysBuilder(RemoteMessage.class).keys());
			Set<TypedKey<?>> tmpKeys = new HashSet<TypedKey<?>>();
			tmpKeys.addAll(list);
			keys = Collections.unmodifiableSet(tmpKeys);
		}

		/**
		 * @inheritDoc
		 * 
		 * @see MessageSubType#getSnaMessageType()
		 */
		@Override
		public SnaMessage.Type getSnaMessageType() {
			return RemoteMessage.TYPE;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see KeysCollection#keys()
		 */
		@Override
		public Set<TypedKey<?>> keys() {
			return this.keys;
		}

		/**
		 * @inheritDoc
		 * 
		 * @see KeysCollection#key(java.lang.String)
		 */
		@Override
		public TypedKey<?> key(String key) {
			TypedKey<?> typedKey = null;

			Iterator<TypedKey<?>> iterator = this.keys.iterator();
			while (iterator.hasNext()) {
				typedKey = iterator.next();
				if (typedKey.equals(key)) {
					break;
				}
				typedKey = null;
			}
			return typedKey;
		}
	}

}
