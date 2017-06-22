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

import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;

/**
 *	Response dedicated {@link SnaMessage}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface SnaResponseMessage<S extends Enum<S> & SnaMessageSubType &  KeysCollection> 
extends SnaErrorfulMessage<S>
{
	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[]
	{
		new TypedKey<JSONObject>(SnaConstants.RESPONSE_KEY , JSONObject.class, false),
		new TypedKey<Integer>(SnaConstants.STATUS_CODE_KEY , int.class, false)
	};
	
	public static final SnaMessage.Type TYPE = SnaMessage.Type.RESPONSE;
	
	JSONObject getResponse();
	
}
