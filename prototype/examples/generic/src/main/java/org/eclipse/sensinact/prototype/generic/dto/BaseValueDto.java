package org.eclipse.sensinact.prototype.generic.dto;

import java.util.Map;

import org.eclipse.sensinact.prototype.annotation.dto.MapAction;
import org.eclipse.sensinact.prototype.annotation.dto.Metadata;
import org.eclipse.sensinact.prototype.annotation.dto.Provider;
import org.eclipse.sensinact.prototype.annotation.dto.Resource;
import org.eclipse.sensinact.prototype.annotation.dto.Service;

/**
 * A base dto for performing updates, suitable for subclassing by device modules
 */
public abstract class BaseValueDto {

	@Provider
	public String provider;
	
	@Service
	public String service;
	
	@Resource
	public String resource;

	@Metadata(onMap = {MapAction.USE_KEYS_AS_FIELDS, MapAction.REMOVE_NULL_VALUES})
	public Map<String, Object> metadata;
	
}
