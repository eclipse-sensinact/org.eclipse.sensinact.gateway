package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaConstants;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface LoginMessage
{
	public static final TypedKey<?>[] PERMANENT_KEYS = new TypedKey[]
	{
		new TypedKey<Long>(SnaConstants.GENERATED_KEY , Long.class, false),
		new TypedKey<Long>(SnaConstants.TIMEOUT_KEY , Long.class, false),
		new TypedKey<String>(SnaConstants.TOKEN_KEY , String.class, false)
	};
}
