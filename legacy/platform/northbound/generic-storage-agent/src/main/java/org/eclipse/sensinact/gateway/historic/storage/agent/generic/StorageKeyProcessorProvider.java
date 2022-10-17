/*********************************************************************
* Copyright (c) 2021 Kentyou
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
