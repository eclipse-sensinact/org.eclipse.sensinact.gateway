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
package org.eclipse.sensinact.studio.web;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

@HttpWhiteboardResource(pattern = StudioWebResources.PATH + "/*", prefix = "/studio-web")
@Component(service = StudioWebResources.class,immediate = true)
public class StudioWebResources {
	public static final String NAME = "org.eclipse.sensinact.studio.web";
	public static final String PATH = "/studio-web";
}
