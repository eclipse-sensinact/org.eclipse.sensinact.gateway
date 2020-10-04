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
package org.eclipse.sensinact.gateway.core.message;

import org.eclipse.sensinact.gateway.core.remote.RemoteCore;

/**
 * Recipient of messages of the system relative to SensiNactResourceModels
 * lifecycle or data value updates
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface LocalAgent extends SnaAgent {
	
	public static final String SNAFILTER_AGENT_SUFFIX_PROPERTY = "org.eclipse.sensinact.gateway.filter.suffix";
	public static final String SNAFILTER_AGENT_TYPES_PROPERTY = "org.eclipse.sensinact.gateway.filter.types";
	public static final String SNAFILTER_AGENT_SENDER_PROPERTY = "org.eclipse.sensinact.gateway.filter.sender";
	public static final String SNAFILTER_AGENT_PATTERN_PROPERTY = "org.eclipse.sensinact.gateway.filter.pattern";
	public static final String SNAFILTER_AGENT_COMPLEMENT_PROPERTY = "org.eclipse.sensinact.gateway.filter.complement";
	public static final String SNAFILTER_AGENT_CONDITIONS_PROPERTY = "org.eclipse.sensinact.gateway.filter.conditions";

	public static final String COMMA = ",";
	public static final String DOT = ".";
	
	/**
	 * Registers this LocalAgent into the {@link RemoteCore} passed as parameter
	 * 
	 * @param remoteCore the {@link RemoteCore} into which register this SnaAgent
	 */
	 void registerRemote(RemoteCore remoteCore);
	
}
