/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.push.dto;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Resource;
import org.eclipse.sensinact.prototype.annotation.dto.Service;

/**
 * This example is a DTO defining two resources with the uris 
 * <code>push_example/simple/count</code> and <code>override_example/simple_override/average</code>
 * 
 * <ul>
 *   <li>The provider and service names are defined as annotations, with fields overriding class level</li>
 *   <li>The resource name is inferred from the data field name unless an annotation defines it.</li>
 *   <li>The resource type is inferred from the data field type unless an annotation defines it.</li>
 * </ul>
 * 
 */
@Provider("push_example")
@Service("simple")
public class _02_SimpleMultiDTO {
	
	@Data
	public int count;
	
	@Provider("override_example")
	@Service("simple_override")
	@Resource("average")
	@Data(type=double.class)
	public Float remapped;

}
