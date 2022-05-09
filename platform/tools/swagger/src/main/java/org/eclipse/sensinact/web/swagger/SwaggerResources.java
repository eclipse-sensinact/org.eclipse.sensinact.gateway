/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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

