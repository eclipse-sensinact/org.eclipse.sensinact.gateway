package org.eclipse.sensinact.prototype.push;
/**
 * Not real, just a thought
 */

import org.osgi.util.promise.Promise;

public interface PrototypePush {
	
	Promise<?> pushUpdate(Object o);

}
