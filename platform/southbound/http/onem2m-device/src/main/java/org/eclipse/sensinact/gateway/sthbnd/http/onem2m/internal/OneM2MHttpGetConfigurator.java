/*
 * Copyright (c) 2018 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.onem2m.internal;

import org.eclipse.sensinact.gateway.sthbnd.http.smpl.HttpTaskConfigurator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class OneM2MHttpGetConfigurator implements HttpTaskConfigurator {
    @Override
    public <T extends HttpTask<?, ?>> void configure(T task) {
        String[] path = task.getPath().split("/");
        if (OneM2MHttpPacketReader.DEFAULT_SERVICE_NAME.equalsIgnoreCase(path[2])) {
            task.setUri(task.getUri() + "/" + path[1] + "/" + path[3] + "/latest");
        } else {
            task.setUri(task.getUri() + "/" + path[1] + "/" + path[2] + "/" + path[3] + "/latest");
        }
        task.addHeaders(new HashMap<String, List<String>>() {{
            put("X-M2M-RI", new ArrayList<String>() {{
                add(UUID.randomUUID().toString());
            }});
        }});
    }
}
