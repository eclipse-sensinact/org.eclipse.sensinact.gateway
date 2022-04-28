/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.studio.web;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

@HttpWhiteboardResource(pattern = StudioWebResources.PATH + "/*", prefix = "/studio-web")
@Component(service = StudioWebResources.class,immediate = true)
public class StudioWebResources {
	public static final String NAME = "org.eclipse.sensinact.studio.web";
	public static final String PATH = "/studio-web";
}
