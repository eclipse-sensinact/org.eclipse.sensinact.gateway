package org.eclipse.sensinact.prototype.push.dto;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Service;

/**
 * This example is a minimal DTO defining a resource with the uri 
 * <code>push_example/simple/count</code>
 * 
 * <ul>
 *   <li>The provider and service names are defined as annotations at class level</li>
 *   <li>The resource name is inferred from the data field name.</li>
 *   <li>The resource type is inferred from the data field type</li>
 * </ul>
 * 
 */
@Provider("push_example")
@Service("simple")
public class _01_SimpleDTO {
	
	@Data
	public int count;

}
