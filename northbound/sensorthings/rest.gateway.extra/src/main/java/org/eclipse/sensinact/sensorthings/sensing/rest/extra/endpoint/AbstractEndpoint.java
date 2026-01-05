package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import static org.eclipse.sensinact.sensorthings.sensing.rest.access.ExpansionSettings.EMPTY;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.filters.api.FilterParserException;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.ExpansionSettings;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IFilterConstants;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.LocationsExtraUseCase;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;

public class AbstractEndpoint<D extends Id> {
    @Context
    protected UriInfo uriInfo;

    @Context
    protected Providers providers;

    @Context
    protected Application application;

    @Context
    protected ContainerRequestContext requestContext;

    private Class<D> type;

    @SuppressWarnings("unchecked")
    public AbstractEndpoint() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<D>) param;
    }

    @SuppressWarnings("unchecked")
    public Response create(D dto) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(type);
        boolean result = useCase.create(getSession(), dto);
        if (result) {
            return Response.accepted().build();
        }
        return Response.status(500).build();
    }

    @SuppressWarnings("unchecked")
    public Response update(String id, D dto) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(type);
        boolean result = useCase.update(getSession(), id, dto);
        if (result) {
            return Response.accepted().build();
        }
        return Response.status(500).build();
    }

    @SuppressWarnings("unchecked")
    public Response delete(String id) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(type);
        boolean result = useCase.delete(getSession(), id);
        if (result) {
            return Response.accepted().build();
        }
        return Response.status(500).build();
    }

    /**
     * Returns a user session
     */
    protected SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    @SuppressWarnings("unchecked")
    protected IExtraUseCase<?> getExtraUseCase(Class<? extends Id> aType) {
        Map<String, IExtraUseCase<?>> map = providers.getContextResolver(Map.class, MediaType.WILDCARD_TYPE)
                .getContext(null);
        return map.get(aType.toString());
    }

    protected LocationsExtraUseCase getLocationsExtraUseCase() {
        return providers.getContextResolver(LocationsExtraUseCase.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    /**
     * Returns an object mapper
     */
    protected ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE).getContext(null);
    }

    protected ExpansionSettings getExpansions() {
        ExpansionSettings es = (ExpansionSettings) requestContext.getProperty(IFilterConstants.EXPAND_SETTINGS_STRING);
        return es == null ? EMPTY : es;
    }

    private ISensorthingsFilterParser getFilterParser() {
        return providers.getContextResolver(ISensorthingsFilterParser.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    protected ICriterion parseFilter(final EFilterContext context) throws WebApplicationException {
        final String filterString = (String) requestContext.getProperty(IFilterConstants.PROP_FILTER_STRING);
        if (filterString == null || filterString.isBlank()) {
            return null;
        }

        try {
            return getFilterParser().parseFilter(filterString, context);
        } catch (FilterParserException e) {
            throw new BadRequestException("Error parsing filter", e);
        }
    }
}
