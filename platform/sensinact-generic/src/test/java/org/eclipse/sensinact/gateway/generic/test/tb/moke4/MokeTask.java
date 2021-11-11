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
package org.eclipse.sensinact.gateway.generic.test.tb.moke4;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.uri.GenericURITask;
import org.eclipse.sensinact.gateway.generic.uri.URITaskTranslator;

/**
 * A Task service for test
 */
public class MokeTask extends GenericURITask {
    /**
     * @param command
     * @param transmitter
     * @param path
     * @param profileId
     * @param resourceConfig
     * @param parameters
     */
    public MokeTask(CommandType command, URITaskTranslator transmitter, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        super(command, transmitter, path, profileId, resourceConfig, parameters);
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.uri.URITask#getContent()
     */
    @Override
    public Object getContent() {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.uri.URITask#getUri()
     */
    @Override
    public String getUri() {
        return null;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.uri.URITask#getOptions()
     */
    @Override
    public Map<String, List<String>> getOptions() {
        final String taskId = getTaskIdentifier();
        return new HashMap<String, List<String>>() {
			private static final long serialVersionUID = 1L;
			{
            this.put("taskId", Collections.<String>singletonList(taskId));
        }};
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.generic.TaskImpl#setResult(java.lang.Object)
     */
    public void setResult(Object result) {
        super.setResult(result);
    }
}
