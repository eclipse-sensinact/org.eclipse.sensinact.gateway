/*
 * Copyright (c) 2021 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.historic.storage.reader.api;

import java.time.ZonedDateTime;

/**
 *
 */
public interface HistoricValueRequest extends HistoricRequest<TemporalDTO> {

	void setHistoricTime(ZonedDateTime time);
	
}
