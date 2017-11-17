package org.eclipse.sensinact.gateway.nthbnd.test.jsonpath;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.internal.Path;
import com.jayway.jsonpath.internal.path.PredicateContextImpl;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.sensinact.gateway.nthbnd.jsonpath.builder.JsonOrgNodeBuilder;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.json.JsonOrgJsonProvider;
import org.eclipse.sensinact.gateway.nthbnd.jsonpath.mapper.JsonOrgMappingProvider;

public class BaseTestConfiguration {

    public static final Configuration JSON_ORG_CONFIGURATION = Configuration
            .builder()
            .mappingProvider(new JsonOrgMappingProvider())
            .jsonProvider(new JsonOrgJsonProvider())
            .nodeBuilder(new JsonOrgNodeBuilder())
            .build();

    public static Iterable<Configuration> configurations() {
        return Arrays.asList(
               JSON_ORG_CONFIGURATION
        );
    }

    public static Iterable<Configuration> objectMappingConfigurations() {
        return Arrays.asList(
                 JSON_ORG_CONFIGURATION
        );
    }

    public static Predicate.PredicateContext createPredicateContext(final Object check) {
        return new PredicateContextImpl(check, check, 
        		Configuration.defaultConfiguration(), 
        		new HashMap<Path, Object>());
    }
}
