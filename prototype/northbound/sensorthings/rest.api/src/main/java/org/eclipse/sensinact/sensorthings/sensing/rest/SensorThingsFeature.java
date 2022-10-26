package org.eclipse.sensinact.sensorthings.sensing.rest;

import org.eclipse.sensinact.sensorthings.sensing.rest.filters.CountFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.ExpandFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.FilterFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.ObjectMapperProvider;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.OrderByFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.PropFilterImpl;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.RefFilterImpl;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.SelectFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.SkipFilter;
import org.eclipse.sensinact.sensorthings.sensing.rest.filters.TopFilter;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

public class SensorThingsFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        // Jackson
        context.register(ObjectMapperProvider.class);
        context.register(JacksonJsonProvider.class);

        // Bound response rewriters
        context.register(PropFilterImpl.class);
        context.register(RefFilterImpl.class);
        
        // Query string handlers
        context.register(CountFilter.class);
        context.register(ExpandFilter.class);
        context.register(FilterFilter.class);
        context.register(OrderByFilter.class);
        context.register(SelectFilter.class);
        context.register(SkipFilter.class);
        context.register(TopFilter.class);
        
        return true;
    }

}
