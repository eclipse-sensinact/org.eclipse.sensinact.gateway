/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.filters.ldap.impl;

import java.util.Map;

import org.eclipse.sensinact.northbound.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.api.IFilterParser;
import org.eclipse.sensinact.northbound.filters.ldap.ILdapFilterConstants;
import org.eclipse.sensinact.prototype.snapshot.ICriterion;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true, service = IFilterParser.class, property = {
        IFilterParser.SUPPORTED_FILTER_LANGUAGE + "=" + ILdapFilterConstants.LDAP_FILTER })
public class LdapFilterComponent implements IFilterParser {

    @Override
    public ICriterion parseFilter(String query, String queryLanguage, Map<String, Object> parameters)
            throws FilterParserException {
        try {
            return LdapParser.parse(query);
        } catch (ParseException e) {
            throw new FilterParserException("Error parsing LDAP query '" + query + "': " + e, e);
        }
    }
}
