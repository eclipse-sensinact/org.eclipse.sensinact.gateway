package org.eclipse.sensinact.prototype.push.dto;

import org.eclipse.sensinact.prototype.annotation.dto.Data;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Resource;
import org.eclipse.sensinact.prototype.annotation.dto.Service;
import org.eclipse.sensinact.prototype.annotation.dto.Timestamp;

/**
 * This example is a DTO defining a resource with the uri <code>foo/bar/foobar</code>
 * 
 * <ul>
 *   <li>The provider, service and resource names are defined as annotated fields</li>
 *   <li>The timestamp is set using a field</li>
 * </ul>
 * 
 */
public class _03_ParameterizedDTO {
	
	@Provider
	public String provider = "foo";
	
	@Service
	public String service = "bar";
	
	@Resource
	public String resource = "foobar";
	
	@Data
	public int count;
	
	@Timestamp
	public long timestamp;

}
