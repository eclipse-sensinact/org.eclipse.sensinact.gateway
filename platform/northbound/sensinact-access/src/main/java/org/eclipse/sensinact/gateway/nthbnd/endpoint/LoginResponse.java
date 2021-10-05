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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.AbstractSnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessageSubType;
import org.eclipse.sensinact.gateway.util.JSONUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A LoginEndpoint is a connection point to a sensiNact instance
 * allowing to create an {@link NorthboundEndpoint} for a specific
 * user or to reactivate an existing one
 *
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class LoginResponse extends AbstractSnaErrorfulMessage<LoginResponse.TokenMode> implements LoginMessage {
    public enum TokenMode implements SnaMessageSubType, KeysCollection {
        TOKEN_CREATION, TOKEN_RENEW;
        final Set<TypedKey<?>> keys;

        TokenMode() {
            List<TypedKey<?>> list = Arrays.asList(new SnaMessage.KeysBuilder(LoginResponse.class).keys());

            Set<TypedKey<?>> tmpKeys = new HashSet<TypedKey<?>>();
            tmpKeys.addAll(list);
            keys = Collections.unmodifiableSet(tmpKeys);
        }

        @Override
        public Set<TypedKey<?>> keys() {
            return this.keys;
        }

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

		@Override
		public Type getSnaMessageType() {
			return Type.RESPONSE;
		}
    }

    /**
     * Constructor
     *
     * @param mediator the {@link NorthboundMediator} that will allow
     *                 the LoginEndpoint to be instantiated to interact with the
     *                 OSGi host environment
     * @throws InvalidCredentialException
     */
    public LoginResponse(NorthboundMediator mediator, TokenMode type) {
        super(mediator, "/login", type);
    }

    /**
     * @return
     */
    public String getToken() {
        return super.<String>get(SnaConstants.TOKEN_KEY);
    }

    /**
     * @return
     */
    protected void setToken(String token) {
        super.put(SnaConstants.TOKEN_KEY, token);
    }

    /**
     * @return
     */
    public long getGenerated() {
        return super.<Long>get(SnaConstants.GENERATED_KEY);
    }

    /**
     * @return
     */
    protected void setGenerated(long generated) {
        super.put(SnaConstants.GENERATED_KEY, generated);
    }

    /**
     * @return
     */
    public long getTimeout() {
        return super.<Long>get(SnaConstants.TIMEOUT_KEY);
    }

    /**
     * @return
     */
    protected void setTimeout(long timeout) {
        super.put(SnaConstants.TIMEOUT_KEY, timeout);
    }
    
    @Override
    public String getJSON() {
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<TypedKey<?>, Object>> iterator = super.properties.entrySet().iterator();

        builder.append(JSONUtils.OPEN_BRACE);
        int index = 0;

        while (iterator.hasNext()) {
            Map.Entry<TypedKey<?>, Object> entry = iterator.next();
            TypedKey<?> typedKey = entry.getKey();
            builder.append(index > 0 ? JSONUtils.COMMA : "");
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
}