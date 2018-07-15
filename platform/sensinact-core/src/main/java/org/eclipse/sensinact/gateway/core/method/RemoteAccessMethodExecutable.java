package org.eclipse.sensinact.gateway.core.method;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.Name;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.RemoteCore;
import org.eclipse.sensinact.gateway.core.RemoteEndpoint;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class RemoteAccessMethodExecutable implements Executable<RemoteCore, JSONObject> {
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

	public RemoteAccessMethodExecutable(Type method, String publicKey) {
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

	protected <P> P get(String name) {
		Object o = this.props.get(new Name<TypedKey<P>>(name));
		try {
			P p = (P) o;
			return p;
		} catch (ClassCastException e) {
		}
		return (P) null;
	}

	protected <P> P get(TypedKey<P> key) {
		P p = (P) this.props.get(key);
		return p;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
	 */
	@Override
	public JSONObject execute(RemoteCore core) throws Exception {
		JSONObject json = null;
		if (core == null) {
			return json;
		}
		RemoteEndpoint endpoint = core.endpoint();
		String provider = serviceProvider.substring(serviceProvider.indexOf(':') + 1);

		switch (this.method.name()) {
		case "ACT":
			json = endpoint.act(publicKey, provider, service, resource, this.<Object[]>get(ARGUMENTS_TK));
			break;
		case "GET":
			json = endpoint.get(publicKey, provider, service, resource, attribute);
			break;
		case "SET":
			json = endpoint.set(publicKey, provider, service, resource, attribute, this.<Object>get(VALUE_TK));
			break;
		case "SUBSCRIBE":
			json = endpoint.subscribe(publicKey, provider, service, resource, this.<Recipient>get(RECIPIENT_TK),
					this.<JSONArray>get(CONDITIONS_TK));
			break;
		case "UNSUBSCRIBE":
			json = endpoint.unsubscribe(publicKey, provider, service, resource, this.<String>get(SUBSCRIPTION_ID_TK));
			break;
		default:
			break;
		}
		return json;
	}
}
