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
package org.eclipse.sensinact.northbound.filters.sensorthings.impl;

import static org.eclipse.sensinact.northbound.filters.sensorthings.ISensorThingsFilterConstants.OGC_FILTER;
import static org.eclipse.sensinact.northbound.filters.sensorthings.ISensorThingsFilterConstants.SENSORTHINGS_FILTER;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.filters.api.IFilterParser;
import org.eclipse.sensinact.filters.propertytypes.FiltersSupported;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorThingsFilterConstants;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterLexer;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolcommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.BoolCommonExprVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.IDtoMemoryCache;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(configurationPid = "sensinact.sensorthings.northbound.rest")
@FiltersSupported({ OGC_FILTER, SENSORTHINGS_FILTER })
public class SensorthingsFilterComponent implements IFilterParser, ISensorthingsFilterParser {

    public static @interface Config {
        String history_provider()

        default NOT_SET;

        int history_results_max()

        default 3000;

        boolean history_in_memory() default false;
    }

    public static final String NOT_SET = "<<NOT_SET>>";

    @Activate
    Config config;

    @Reference
    SensiNactSessionManager sessionManager;

    @Reference(target = "(cache.type=expanded-observation)")
    IDtoMemoryCache<ExpandedObservation> cacheObs;

    @Reference(target = "(cache.type=historical-location)")
    IDtoMemoryCache<Instant> cacheHl;

    SensiNactSession session;

    public SensiNactSession getSession() {
        if (session == null || session.isExpired())
            session = sessionManager.getDefaultSession(UserInfo.ANONYMOUS);
        return session;
    }

    public void setSession(SensiNactSession session) {
        this.session = session;
    }

    @Override
    public ICriterion parseFilter(String query, String queryLanguage, Map<String, Object> parameters)
            throws FilterParserException {

        EFilterContext context = null;
        Object rawContext = parameters.get(ISensorThingsFilterConstants.PARAM_CONTEXT);
        if (rawContext instanceof EFilterContext) {
            context = (EFilterContext) rawContext;
        } else if (rawContext instanceof String) {
            context = EFilterContext.valueOf((String) rawContext);
        }

        if (context == null) {
            throw new FilterParserException("Can't parse a SensorThings filter without a context");
        }

        // Return the ICriterion
        return parseFilter(query, context);
    }

    private volatile Map<String, Object> dynamicProps = new HashMap<>();

    @Activate
    @Modified
    protected void update(Map<String, Object> properties) {
        this.dynamicProps = properties;
    }

    public boolean isHistoryMemory() {
        return dynamicProps.containsKey("history.in.memory")
                ? Boolean.parseBoolean(String.valueOf(dynamicProps.get("history.in.memory")))
                : config.history_in_memory();
    }

    @Override
    public ICriterion parseFilter(String query, EFilterContext filterContext) throws FilterParserException {
        final Predicate<ResourceValueFilterInputHolder> predicate;

        // Parse the filter
        try {
            final CharStream inStream = CharStreams.fromString(query);
            final ODataFilterLexer markupLexer = new ODataFilterLexer(inStream);
            final CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
            final ODataFilterParser parser = new ODataFilterParser(commonTokenStream);
            final BoolcommonexprContext parsedContext = parser.boolcommonexpr();
            final BoolCommonExprVisitor visitor = new BoolCommonExprVisitor(parser);
            predicate = visitor.visit(parsedContext);
        } catch (Exception e) {
            throw new FilterParserException("Error parsing SensorThings query '" + query + "': " + e, e);
        }

        // Return the ICriterion
        return new SensorthingsCriterion(filterContext, getSession(), predicate, dynamicProps,
                isHistoryMemory() ? cacheObs : null, isHistoryMemory() ? cacheHl : null);
    }
}
