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
package org.eclipse.sensinact.filters.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.filters.api.IFilterHandler;
import org.eclipse.sensinact.filters.api.IFilterParser;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Handler to ease usage of filters
 */
@Component(immediate = true, service = IFilterHandler.class)
public class FilterHandler implements IFilterHandler {

    @Activate
    private BundleContext context;

    @Override
    public ICriterion parseFilter(final String filterLanguage, final String filterQuery,
            final Map<String, Object> parameters) throws FilterParserException {

        final ServiceReference<IFilterParser> svcRef = findParser(filterLanguage);
        try {
            final IFilterParser parser = context.getService(svcRef);
            return parser.parseFilter(filterQuery, filterLanguage, parameters);
        } finally {
            context.ungetService(svcRef);
        }
    }

    private ServiceReference<IFilterParser> findParser(final String filterLanguage) throws FilterParserException {
        final Collection<ServiceReference<IFilterParser>> svcRefs;
        try {
            svcRefs = context.getServiceReferences(IFilterParser.class,
                    String.format("(%s=%s)", IFilterParser.SUPPORTED_FILTER_LANGUAGE, filterLanguage));
        } catch (InvalidSyntaxException e) {
            throw new FilterParserException(
                    String.format("Invalid filter language '%s': %s'", filterLanguage, e.getMessage()), e);
        }

        if (svcRefs == null || svcRefs.isEmpty()) {
            throw new FilterParserException(String.format("No parser for filter language '%s'", filterLanguage));
        }

        return svcRefs.stream().sorted(Comparator.naturalOrder()).findFirst().get();
    }
}
