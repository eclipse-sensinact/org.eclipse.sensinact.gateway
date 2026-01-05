package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.dto;

import java.util.Objects;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.gateway.sensorthings.models.utils.DtoMapperUtils;
import org.eclipse.sensinact.northbound.session.SensiNactSession;

import jakarta.ws.rs.core.UriInfo;

public class DtoMapper {

    public static ThingExtra toThingCreate(SensiNactSession userSession, UriInfo uriInfo, ProviderSnapshot provider) {
        ThingExtra thing = new ThingExtra();
        thing.id = provider.getName();

        String friendlyName = DtoMapperUtils
                .toString(DtoMapperUtils.getProviderAdminFieldValue(provider, DtoMapperUtils.FRIENDLY_NAME));
        thing.name = Objects.requireNonNullElse(friendlyName, provider.getName());

        String description = DtoMapperUtils
                .toString(DtoMapperUtils.getProviderAdminFieldValue(provider, DtoMapperUtils.DESCRIPTION));
        thing.description = Objects.requireNonNullElse(description, DtoMapperUtils.NO_DESCRIPTION);

        thing.selfLink = uriInfo.getBaseUriBuilder().path(DtoMapperUtils.VERSION).path("Things({id})")
                .resolveTemplate("id", thing.id).build().toString();
        thing.datastreamsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Datastreams").build().toString();
        thing.historicalLocationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("HistoricalLocations")
                .build().toString();
        thing.locationsLink = uriInfo.getBaseUriBuilder().uri(thing.selfLink).path("Locations").build().toString();

        return thing;
    }
}
