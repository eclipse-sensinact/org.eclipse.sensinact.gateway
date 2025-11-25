package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessProviderUseCase;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.dto.DtoMapper;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.dto.ThingExtra;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.core.UriInfo;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class ThingsExtraUseCase extends AbstractExtraUseCase<ThingExtra> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessProviderUseCase providerUseCase;

    ThingExtraModel toUpdates(ThingExtra thing) {
        String providerId = sanitizeId(thing.name == null ? thing.id : thing.name);
        GeoJsonObject location = null;
        Map<String, Object> thingProperties = thing.properties != null
                ? thing.properties.entrySet().stream()
                        .collect(toMap(e -> "sensorthings.thing." + e.getKey(), Entry::getValue))
                : null;

        return new ThingExtraModel(providerId, thing.name, thing.description, location, thing.id, thingProperties);

    }

    @Service("admin")
    public record ThingExtraModel(@Model EClass model, @Provider String providerId,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT) String friendlyName,
            @Service("thing") @Data(onDuplicate = UPDATE_IF_DIFFERENT) String description,
            @Data(onDuplicate = UPDATE_IF_DIFFERENT) GeoJsonObject location,
            @Service("thing") @Resource("id") @Data(onDuplicate = UPDATE_IF_DIFFERENT) Object thingId,
            @Service("thing") @Resource("id") @Metadata(onMap = {
                    USE_KEYS_AS_FIELDS }) Map<String, Object> properties){
        public ThingExtraModel {
            if (model == null) {
                model = SENSOR_THINGS_DEVICE;
            }
            if (model != SENSOR_THINGS_DEVICE) {
                throw new IllegalArgumentException(
                        "The model for the provider must be " + SENSOR_THINGS_DEVICE.getName());
            }
        }

        ThingExtraModel(String providerId, String friendlyName, String description, GeoJsonObject location,
                Object thingId, Map<String, Object> properties) {
            this(SENSOR_THINGS_DEVICE, providerId, friendlyName, description, location, thingId, properties);
        }
    }

    @Override
    public ExtraUseCaseResponse<ThingExtra> create(SensiNactSession session, UriInfo urlInfo, ThingExtra dto) {
        // call create
        try {
            ThingExtraModel model = toUpdates(dto);
            Object obj = dataUpdate.pushUpdate(model).getValue();
            ProviderSnapshot provider = providerUseCase.read(session, model.providerId);
            ThingExtra thing = DtoMapper.toThingCreate(session, urlInfo, provider);
            return new ExtraUseCaseResponse<ThingExtra>((String) thing.id, thing);

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ThingExtra>(false, "fail to get providerSnapshot");

        }
    }

    @Override
    public ExtraUseCaseResponse<ThingExtra> update(SensiNactSession session, UriInfo urlInfo, String id,
            ThingExtra dto) {
        return new ExtraUseCaseResponse<ThingExtra>(false, "not implemented");

    }

    @Override
    public ExtraUseCaseResponse<ThingExtra> delete(SensiNactSession session, UriInfo urlInfo, String id) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<ThingExtra>(false, "not implemented");
    }

    @Override
    public ExtraUseCaseResponse<ThingExtra> patch(SensiNactSession session, UriInfo urlInfo, String id,
            ThingExtra dto) {
        // TODO Auto-generated method stub
        return new ExtraUseCaseResponse<ThingExtra>(false, "not implemented");
    }
}
