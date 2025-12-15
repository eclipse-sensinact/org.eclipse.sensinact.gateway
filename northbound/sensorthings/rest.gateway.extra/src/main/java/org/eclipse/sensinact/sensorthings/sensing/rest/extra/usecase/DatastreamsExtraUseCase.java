package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.SensorThingsUpdate;
import org.eclipse.sensinact.sensorthings.sensing.rest.access.IAccessServiceUseCase;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.BadRequestException;

/**
 * UseCase that manage the create, update, delete use case for sensorthing
 * object
 */
@Component(service = IExtraUseCase.class)
public class DatastreamsExtraUseCase extends AbstractExtraUseCase<ExpandedDataStream, ServiceSnapshot> {

    @Reference
    DataUpdate dataUpdate;

    @Reference
    IAccessServiceUseCase serviceUseCase;

    @Reference
    ISensorExtraUseCase sensorExtraUseCase;

    @Reference
    IObservedPropertyExtraUseCase observedPropertyUseCase;

    public ExtraUseCaseResponse<ServiceSnapshot> create(ExtraUseCaseRequest<ExpandedDataStream> request) {
        String id = getId(request.model());
        List<SensorThingsUpdate> listDtoModels = toDtos(request);

        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, e, "fail to create");

        }
        String thingId = request.model().thing().id() == null ? request.parentId()
                : (String) request.model().thing().id();
        if (thingId == null) {
            throw new BadRequestException("Thing id not found");
        }
        ServiceSnapshot snapshot = serviceUseCase.read(request.session(), thingId, id);
        if (snapshot != null) {

            return new ExtraUseCaseResponse<ServiceSnapshot>(id, snapshot);
        }
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get Snapshot");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> delete(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerSnapshot");

    }

    public ExtraUseCaseResponse<ServiceSnapshot> patch(ExtraUseCaseRequest<ExpandedDataStream> request) {
        return new ExtraUseCaseResponse<ServiceSnapshot>(false, "fail to get providerSnapshot");

    }

    @Override
    protected List<SensorThingsUpdate> toDtos(ExtraUseCaseRequest<ExpandedDataStream> request) {
        // read thing for each location and update it
        ExpandedDataStream datastream = request.model();

        if (datastream.thing() == null) {
            throw new UnsupportedOperationException(String.format("Thing id not found in Datastream Payload"));
        }
        String providerId = request.model().thing() != null ? (String) request.model().thing().id()
                : request.parentId();
        if (providerId == null) {
            throw new BadRequestException("Thing id not found");
        }

        ExpandedSensor sensor = null;
        // retrieve created sensor
        if (datastream.sensor() != null && DtoMapper.isRecordOnlyField(datastream.sensor(), "id")) {
            String idSensor = getIdFromRecord(datastream.sensor());

            sensor = sensorExtraUseCase.getInMemorySensor(idSensor);

        }
        ExpandedObservedProperty observedProperty = null;
        // retrieve create observedPorperty
        if (datastream.observedProperty() != null && DtoMapper.isRecordOnlyField(datastream.observedProperty(), "id")) {
            String idObservedProperty = getIdFromRecord(datastream.observedProperty());
            observedProperty = observedPropertyUseCase.getInMemoryObservedProperty(idObservedProperty);
        }
        UnitOfMeasurement unit = datastream.unitOfMeasurement();
        return DtoMapper.toDatastreamUpdate(providerId, datastream, sensor, observedProperty, unit);

    }

    private String getIdFromRecord(Object record) {
        Object field = DtoMapper.getRecordField(record, "id");
        Object id = null;
        if (field instanceof Map) {
            id = ((Map<?, ?>) field).values().stream().findFirst().get();
        }
        return id instanceof String ? (String) id : null;

    }

    public ExtraUseCaseResponse<ServiceSnapshot> update(ExtraUseCaseRequest<ExpandedDataStream> request) {
        List<SensorThingsUpdate> listDtoModels = toDtos(request);
        String id = getId(request.model());
        String thingId = request.model().thing().id() == null ? request.parentId()
                : (String) request.model().thing().id();
        if (thingId == null) {
            throw new BadRequestException("Thing id not found");
        }
        // update/create provider
        try {
            dataUpdate.pushUpdate(listDtoModels).getValue();

        } catch (InvocationTargetException | InterruptedException e) {
            return new ExtraUseCaseResponse<ServiceSnapshot>(false, e, "fail to create");
        }

        ServiceSnapshot serviceSnapshot = serviceUseCase.read(request.session(), thingId, id);
        return new ExtraUseCaseResponse<ServiceSnapshot>(id, serviceSnapshot);

    }

    @Override
    public String getId(ExpandedDataStream dto) {
        return (String) (dto.id() != null ? dto.id() : dto.name());
    }

}
