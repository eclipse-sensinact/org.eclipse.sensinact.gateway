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

import org.eclipse.sensinact.gateway.api.message.AbstractSnaErrorfulMessage;
import org.eclipse.sensinact.gateway.api.message.SnaMessage;
import org.eclipse.sensinact.gateway.api.message.MessageSubType;
import org.eclipse.sensinact.gateway.api.message.ResponseMessage;
import org.eclipse.sensinact.gateway.common.props.KeysCollection;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;
import org.eclipse.sensinact.gateway.util.JSONUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A UserResponse 
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RegisteringResponse extends AbstractSnaErrorfulMessage<RegisteringResponse.RegisteringRequest> implements RegisteringMessage {
    
	public enum RegisteringRequest implements MessageSubType, KeysCollection {
        USER_CREATION, PASSWORD_RENEW;
        final Set<TypedKey<?>> keys;

        RegisteringRequest() {
            List<TypedKey<?>> list = Arrays.asList(new SnaMessage.KeysBuilder(RegisteringResponse.class).keys());

            Set<TypedKey<?>> tmpKeys = new HashSet<TypedKey<?>>();
            tmpKeys.addAll(list);
            keys = Collections.unmodifiableSet(tmpKeys);
        }

        /**
         * @inheritDoc
         * @see MessageSubType#getSnaMessageType()
         */
        @Override
        public SnaMessage.Type getSnaMessageType() {
            return ResponseMessage.TYPE;
        }

        /**
         * @inheritDoc
         * @see KeysCollection#keys()
         */
        @Override
        public Set<TypedKey<?>> keys() {
            return this.keys;
        }

        /**
         * @inheritDoc
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

    /**
     * Constructor
     *
     * @param mediator the {@link NorthboundMediator} that will allow
     *                 the LoginEndpoint to be instantiated to interact with the
     *                 OSGi host environment
     * @throws InvalidCredentialException
     */
    public RegisteringResponse(NorthboundMediator mediator, RegisteringRequest type) {
        super(mediator, "/register", type);
    }
    
    /**
     * @return
     */
    public String getMessage() {
        return super.<String>get(SnaConstants.MESSAGE_KEY);
    }

    /**
     * @return
     */
    protected void setMessage(String message) {
        super.put(SnaConstants.MESSAGE_KEY, message);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.common.props.TypedProperties#getJSON()
     */
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