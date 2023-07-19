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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.northbound.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.api.IFilterParser;
import org.eclipse.sensinact.northbound.filters.ldap.ILdapFilterConstants;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterLexer;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.LdapFilterParser.FilterContext;
import org.eclipse.sensinact.northbound.filters.ldap.antlr.impl.FilterVisitor;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the LDAP filter parser service
 */
@Component(immediate = true, service = IFilterParser.class, property = {
        IFilterParser.SUPPORTED_FILTER_LANGUAGE + "=" + ILdapFilterConstants.LDAP_FILTER })
public class LdapFilterComponent implements IFilterParser {

    @Override
    public ICriterion parseFilter(String query, String queryLanguage, Map<String, Object> parameters)
            throws FilterParserException {

        // Parse the filter
        try {
            final CharStream inStream = CharStreams.fromString(query);
            final LdapFilterLexer markupLexer = new LdapFilterLexer(inStream);
            final CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
            final LdapFilterParser parser = new LdapFilterParser(commonTokenStream);
            final FilterContext parsedContext = parser.filter();
            final FilterVisitor visitor = new FilterVisitor(parser);
            return visitor.visit(parsedContext);
        } catch (Exception e) {
            throw new FilterParserException("Error parsing LDAP query '" + query + "': " + e, e);
        }
    }
}
