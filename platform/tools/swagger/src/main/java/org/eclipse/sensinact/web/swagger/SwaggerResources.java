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
package org.eclipse.sensinact.web.swagger;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardContextSelect;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;
@ServiceRanking(3)
@HttpWhiteboardContextSelect("("+HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME+"="+SwaggerServletContextHelper.NAME+")")
@HttpWhiteboardResource(pattern =  "/*", prefix = "/swagger-api")
@Component(service = SwaggerResources.class,immediate = true)
public class SwaggerResources {
	
}

