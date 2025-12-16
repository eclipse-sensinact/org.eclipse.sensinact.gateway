package org.eclipse.sensinact.sensorthings.sensing.rest.extra.usecase;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
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

        ExpandedSensor sensor = getExpandedSensor(datastream);
        ExpandedObservedProperty observedProperty = getExpandedObservedProperty(datastream);
        UnitOfMeasurement unit = datastream.unitOfMeasurement();

        if (datastream.observations() != null && datastream.observations().size() > 0) {
            return datastream.observations().stream().map(obs -> DtoToModelMapper.toDatastreamUpdate(providerId,
                    datastream, sensor, observedProperty, unit, obs, obs.featureOfInterest())).toList();
        } else {
            return List.of(DtoToModelMapper.toDatastreamUpdate(providerId, datastream, sensor, observedProperty, unit,
                    null, null));
        }

    }

    private ExpandedSensor getExpandedSensor(ExpandedDataStream datastream) {
        ExpandedSensor sensor = null;
        // retrieve created sensor
        if (datastream.sensor() != null && DtoToModelMapper.isRecordOnlyField(datastream.sensor(), "id")) {
            String idSensor = DtoToModelMapper.getIdFromRecord(datastream.sensor());

            sensor = sensorExtraUseCase.getInMemorySensor(idSensor);

        } else {
            sensor = datastream.sensor();
        }
        return sensor;
    }

    private void removeExpandedSensor(ExpandedDataStream datastream) {
        // retrieve created sensor
        if (datastream.sensor() != null && DtoToModelMapper.isRecordOnlyField(datastream.sensor(), "id")) {
            String idSensor = DtoToModelMapper.getIdFromRecord(datastream.sensor());

            sensorExtraUseCase.removeInMemorySensor(idSensor);

        }

    }

    private ExpandedObservedProperty getExpandedObservedProperty(ExpandedDataStream datastream) {
        ExpandedObservedProperty observedProperty = null;
        // retrieve create observedPorperty
        if (datastream.observedProperty() != null
                && DtoToModelMapper.isRecordOnlyField(datastream.observedProperty(), "id")) {
            String idObservedProperty = DtoToModelMapper.getIdFromRecord(datastream.observedProperty());
            observedProperty = observedPropertyUseCase.getInMemoryObservedProperty(idObservedProperty);
        } else {
            observedProperty = datastream.observedProperty();
        }
        return observedProperty;
    }

    private void removeExpandedObservedProperty(ExpandedDataStream datastream) {
        // retrieve create observedPorperty
        if (datastream.observedProperty() != null
                && DtoToModelMapper.isRecordOnlyField(datastream.observedProperty(), "id")) {
            String idObservedProperty = DtoToModelMapper.getIdFromRecord(datastream.observedProperty());
            observedPropertyUseCase.removeInMemoryObservedProperty(idObservedProperty);
        }

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

        removeExpandedObservedProperty(request.model());
        removeExpandedSensor(request.model());

        return new ExtraUseCaseResponse<ServiceSnapshot>(id, serviceSnapshot);

    }

    @Override
    public String getId(ExpandedDataStream dto) {
        return (String) (dto.id() != null ? dto.id() : dto.name());
    }

}
