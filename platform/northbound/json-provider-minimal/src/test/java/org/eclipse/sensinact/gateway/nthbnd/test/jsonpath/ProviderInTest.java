package org.eclipse.sensinact.gateway.nthbnd.test.jsonpath;

import org.assertj.core.util.Lists;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.json.JsonOrgJsonProvider;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.mapper.JsonOrgMappingProvider;
import org.junit.Test;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProviderInTest {
    private final String JSON = "[{\"foo\": \"bar\"}, {\"foo\": \"baz\"}]";
    private final String EQUALS_FILTER = "$.[?(@.foo == %s)].foo";
    private final String IN_FILTER = "$.[?(@.foo in [%s])].foo";
    private final String DOUBLE_QUOTES = "\"bar\"";
    private final String DOUBLE_QUOTES_EQUALS_FILTER = String.format(EQUALS_FILTER, DOUBLE_QUOTES);
    private final String DOUBLE_QUOTES_IN_FILTER = String.format(IN_FILTER, DOUBLE_QUOTES);
    private final String SINGLE_QUOTES = "'bar'";
    private final String SINGLE_QUOTES_EQUALS_FILTER = String.format(EQUALS_FILTER, SINGLE_QUOTES);
    private final String SINGLE_QUOTES_IN_FILTER = String.format(IN_FILTER, SINGLE_QUOTES);


    @Test
    public void testJsonPathQuotesJsonOrg() throws Exception {
        final Configuration jsonOrg = Configuration.builder().jsonProvider(new JsonOrgJsonProvider()).mappingProvider(new JsonOrgMappingProvider()).build();
        final DocumentContext ctx = JsonPath.using(jsonOrg).parse(JSON, false);

        final org.json.JSONArray doubleQuoteEqualsResult = ctx.read(DOUBLE_QUOTES_EQUALS_FILTER);
        assertEquals("bar", doubleQuoteEqualsResult.get(0));

        final org.json.JSONArray singleQuoteEqualsResult = ctx.read(SINGLE_QUOTES_EQUALS_FILTER);
        assertEquals(doubleQuoteEqualsResult.get(0), singleQuoteEqualsResult.get(0));

        final org.json.JSONArray doubleQuoteInResult = ctx.read(DOUBLE_QUOTES_IN_FILTER);
        assertEquals(doubleQuoteInResult.get(0), doubleQuoteEqualsResult.get(0));

        final org.json.JSONArray singleQuoteInResult = ctx.read(SINGLE_QUOTES_IN_FILTER);
        assertEquals(doubleQuoteInResult.get(0), singleQuoteInResult.get(0));
    }

}