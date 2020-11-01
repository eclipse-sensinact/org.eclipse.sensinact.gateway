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
package org.osgi.service.log;

import org.osgi.annotation.versioning.ProviderType;

/**
 * This is a stripped down copy of the LogService 1.4 API (R7). It's
 * trimmed down to the methods used by the optional support for
 * R7 logging.
 */
@ProviderType
public interface LoggerFactory {
    <L extends Logger> L getLogger(String name, Class<L> loggerType);
}
