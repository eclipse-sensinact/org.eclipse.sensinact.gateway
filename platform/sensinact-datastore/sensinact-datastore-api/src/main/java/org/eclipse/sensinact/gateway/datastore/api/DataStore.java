package org.eclipse.sensinact.gateway.datastore.api;

import org.osgi.service.component.annotations.ComponentPropertyType;

@ComponentPropertyType
public @interface DataStore {
	public static final String PREFIX_="org.eclipse.sensinact.";
	
	String provider();
	String sgbd();
}
