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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;

/**
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface LoginMessage {
	
    public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[]{
        new TypedKey<Long>(SnaConstants.GENERATED_KEY, Long.class, false), 
        new TypedKey<Long>(SnaConstants.TIMEOUT_KEY, Long.class, false), 
        new TypedKey<String>(SnaConstants.TOKEN_KEY, String.class, false)
    };
}
