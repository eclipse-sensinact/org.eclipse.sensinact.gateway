/**
 * 
 */
package org.eclipse.sensinact.gateway.historic.storage.agent.generic;

import java.util.Map;

import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * Provide pre-processing applying on storage keys
 */
public interface StorageKeyProcessorProvider {

	/**
	 * Returns the Set of pre-processings applying mapped to their related key
	 * 
	 * @return the Map of keys and pre-processors
	 */
	Map<String, Executable<SnaMessage<?>, Object>> getStorageKeyProcessors();

}
