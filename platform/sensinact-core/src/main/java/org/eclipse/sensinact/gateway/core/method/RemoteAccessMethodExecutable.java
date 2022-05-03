/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.core.remote.SensinactCoreBaseIface;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RemoteAccessMethodExecutable implements Executable<SensinactCoreBaseIface, String> {
	public static final String ARGUMENTS = "arguments";
	public static final String SUBSCRIPTION_ID = "subscriptionId";
	public static final String CONDITIONS = "conditions";
	public static final String RECIPIENT = "recipient";
	public static final String VALUE = "value";

	public static final TypedKey<Object[]> ARGUMENTS_TK = new TypedKey<Object[]>(ARGUMENTS, Object[].class, false);
	public static final TypedKey<String> SUBSCRIPTION_ID_TK = new TypedKey<String>(SUBSCRIPTION_ID, String.class,
			false);
	public static final TypedKey<JSONArray> CONDITIONS_TK = new TypedKey<JSONArray>(CONDITIONS, JSONArray.class, false);
	public static final TypedKey<Recipient> RECIPIENT_TK = new TypedKey<Recipient>(RECIPIENT, Recipient.class, false);
	public static final TypedKey<Object> VALUE_TK = new TypedKey<Object>(VALUE, Object.class, false);

	private Type method;
	private String publicKey;

	private String serviceProvider;
	private String resource;
	private String service;
	private String attribute;

	private Map<TypedKey<?>, Object> props;
	private Mediator mediator;

	public RemoteAccessMethodExecutable(Mediator mediator,Type method, String publicKey) {
		this.mediator = mediator;
		this.method = method;
		this.publicKey = publicKey;
		this.props = new HashMap<TypedKey<?>, Object>();
	}

	public Type getMethod() {
		return this.method;
	}

	public RemoteAccessMethodExecutable withServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
		return this;
	}

	public RemoteAccessMethodExecutable withService(String service) {
		this.service = service;
		return this;
	}

	public RemoteAccessMethodExecutable withResource(String resource) {
		this.resource = resource;
		return this;
	}

	public RemoteAccessMethodExecutable withAttribute(String attribute) {
		this.attribute = attribute;
		return this;
	}

	public <P> RemoteAccessMethodExecutable with(String name, Class<P> type, P prop) {
		TypedKey<P> key = new TypedKey<P>(name, type, false);
		this.props.put(key, prop);
		return this;
	}

	public <P> RemoteAccessMethodExecutable with(TypedKey<P> key, P prop) {
		this.props.put(key, prop);
		return this;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
	 */
	@Override
	public String execute(SensinactCoreBaseIface core) throws Exception {
		String json = null;
		if (core == null) {
			return json;
		}
		String provider = serviceProvider.substring(serviceProvider.indexOf(':') + 1);

		switch (this.method.name()) {
		case "ACT":
			json = core.act(publicKey, provider, service, resource, CastUtils.cast(
					JSONArray.class, this.props.get(ARGUMENTS_TK)).toString());
			break;
		case "GET":
			json = core.get(publicKey, provider, service, resource, attribute);
			break;
		case "SET":
			json = core.set(publicKey, provider, service, resource, attribute, CastUtils.cast(
					JSONArray.class, this.props.get(VALUE_TK)).toString());
			break;
		case "SUBSCRIBE":
		case "UNSUBSCRIBE":
			break;
		default:
			break;
		}
		return json;
	}
}
