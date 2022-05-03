/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;

/**
 * Response dedicated {@link SnaMessage}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaResponseMessage<T, S extends Enum<S> & SnaMessageSubType & KeysCollection>
		extends SnaErrorfulMessage<S> {
	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[] {
			new TypedKey<Object>(SnaConstants.RESPONSE_KEY, Object.class, false),
			new TypedKey<Integer>(SnaConstants.STATUS_CODE_KEY, int.class, false) };

	public static final SnaMessage.Type TYPE = SnaMessage.Type.RESPONSE;

	T getResponse();

}
