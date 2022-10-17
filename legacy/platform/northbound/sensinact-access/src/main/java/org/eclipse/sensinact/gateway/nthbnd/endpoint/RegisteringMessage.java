/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;

/**
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public interface RegisteringMessage {
    public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[]{
    		new TypedKey<String>(SnaConstants.MESSAGE_KEY, String.class, false)};
}
