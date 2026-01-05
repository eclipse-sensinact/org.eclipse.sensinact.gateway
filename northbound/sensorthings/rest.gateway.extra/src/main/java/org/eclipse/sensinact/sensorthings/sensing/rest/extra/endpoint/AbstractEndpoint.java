package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import java.lang.reflect.ParameterizedType;
import java.net.URI;

import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.ExtraUseCasesProvider.ExtraRegistry;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.IExtraUseCase.ExtraUseCaseResponse;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase.LocationsExtraUseCase;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.POST;
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

    private Class<D> type;

    @SuppressWarnings("unchecked")
    public AbstractEndpoint() {
        var superclass = getClass().getGenericSuperclass();
        var param = ((ParameterizedType) superclass).getActualTypeArguments()[0];
        this.type = (Class<D>) param;
    }

    @POST
    @SuppressWarnings("unchecked")
    public Response create(D dto) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(type);
        ExtraUseCaseResponse<D> result = useCase.create(getSession(), uriInfo, dto);
        if (result.success()) {
            URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(result.dto().id)).build();
            return Response.created(createdUri).entity(result.dto()).build();
        }
        return Response.status(500).build();
    }

    @SuppressWarnings("unchecked")
    public Response update(String id, D dto) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(type);
        ExtraUseCaseResponse<D> result = useCase.update(getSession(), uriInfo, id, dto);
        if (result.success()) {
            URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(result.dto().id)).build();
            return Response.accepted(createdUri).entity(result.dto()).build();
        }
        return Response.status(500).build();
    }

    @SuppressWarnings("unchecked")
    public Response delete(String id) {
        IExtraUseCase<D> useCase = (IExtraUseCase<D>) getExtraUseCase(type);
        ExtraUseCaseResponse<D> result = useCase.delete(getSession(), uriInfo, id);
        if (result.success()) {
            URI createdUri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(result.dto().id)).build();
            return Response.created(createdUri).entity(result.dto()).build();
        }
        return Response.status(500).build();
    }

    /**
     * Returns a user session
     */
    protected SensiNactSession getSession() {
        return providers.getContextResolver(SensiNactSession.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    protected IExtraUseCase<?> getExtraUseCase(Class<? extends Id> aType) {
        ExtraRegistry useCaseRegistry = providers.getContextResolver(ExtraRegistry.class, MediaType.WILDCARD_TYPE)
                .getContext(null);
        return useCaseRegistry.getMap().get(aType.getName());
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

}
