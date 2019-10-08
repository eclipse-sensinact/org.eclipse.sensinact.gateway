/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.parser;

public interface RootXmlParsingContext {

    public static final String RESOURCE_INFO_TYPE_PROPERTY = "resourceInfoProperty".intern();
    public static final String RESOURCE_INFO_TYPE_SENSOR = "resourceInfoSensor".intern();
    public static final String RESOURCE_INFO_TYPE_VARIABLE = "resourceInfoVariable".intern();
    public static final String RESOURCE_INFO_TYPE_ACTION = "resourceInfoAction".intern();
    
	void registerProfile(String[] profiles, String[] targets);

	PolicyDefinition getPolicy(String name);

}
