package org.eclipse.sensinact.prototype.model;

/**
 * A common super-interface for modelled types
 * @author timothyjward
 *
 */
public interface Modelled {
	
	String getName();
	
	boolean isExclusivelyOwned();
	
	boolean isAutoDelete();
	
}
