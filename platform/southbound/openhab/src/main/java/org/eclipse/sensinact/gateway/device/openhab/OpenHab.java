/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.device.openhab;

import org.eclipse.sensinact.gateway.device.openhab.internal.OpenHabItem;

import java.util.Set;

/**
 * @Author Jander Nascimento<Jander.BotelhodoNascimento@cea.fr>
 */
public interface OpenHab {

    void deviceHeartBeat(Set<OpenHabItem> items);
    String getIp();
    Integer getPort();
    String getOpenHabURL();

}
