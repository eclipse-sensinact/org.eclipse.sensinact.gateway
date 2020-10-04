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
package ${package}.app.service;

import org.eclipse.sensinact.gateway.core.message.SnaMessage;

/**
 * A WebAppRelay is in charge of relaying received {@link SnaMessage}
 */
public interface WebAppRelay {

	/**
	 * Relays the received {@link SnaMessage}
	 * 
	 * @param message the {@link SnaMessage} to be relayed
	 */
	void relay(SnaMessage<?> message);
}
