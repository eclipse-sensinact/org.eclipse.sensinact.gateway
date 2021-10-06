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
package org.eclipse.sensinact.gateway.nthbnd.filter.attributes;

import java.util.Arrays;

import org.eclipse.sensinact.gateway.core.filtering.Filtering;
import org.eclipse.sensinact.gateway.core.filtering.FilteringType;
import org.osgi.service.component.annotations.Component;

/**
 * {@link Filtering} implementation allowing to search for specific attributes
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@FilteringType(AttributesFiltering.ATTRS)
@Component(immediate=true, service = Filtering.class)
public class AttributesFiltering implements Filtering {
    public static final String ATTRS = "attrs";

	@Override
    public boolean handle(String type) {
        return ATTRS.equals(type);
    }

    @Override
    public String apply(String definition, Object result) {
        return String.valueOf(result);
    }

    @Override
    public String getLDAPComponent(String definition) {
    	String definitionContent = definition;
    	
    	if(definition.startsWith("{") ||definition.startsWith("[") ) 
    		definitionContent = definition.substring(1, definition.length()-1);
    	
    	final String[] defs = definitionContent.split(",");
    	
    	StringBuilder builder = Arrays.asList(defs).stream(
    	).<StringBuilder>collect(
    		()->{return new StringBuilder();},
    		(sb,s)->{
    			if(sb.length()==0 && defs.length>1)
    				sb.append("(&");
    			sb.append("(admin.");
    			sb.append(s);
    			sb.append(".value=*)");
    		},
    		(sb1,sb2)-> {sb1.append(sb2.toString());});

		if(defs.length>1)
			builder.append(")");
        return builder.toString();
    }
}
