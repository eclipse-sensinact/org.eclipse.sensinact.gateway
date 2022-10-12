package org.eclipse.sensinact.prototype.impl;

import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.command.SensinactResource;
import org.eclipse.sensinact.prototype.dto.impl.DataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetValueCommand extends AbstractSensinactCommand<Void> {
	
	private final DataUpdateDto dataUpdateDto;
	
	public SetValueCommand(DataUpdateDto dataUpdateDto) {
		this.dataUpdateDto = dataUpdateDto;
	}

	@Override
	protected Promise<Void> call(SensinactModel model, PromiseFactory promiseFactory) {
		
		SensinactResource resource = model.getOrCreateResource(dataUpdateDto.model, dataUpdateDto.provider, 
				dataUpdateDto.service, dataUpdateDto.resource, dataUpdateDto.type);
		
		return resource.setValue(dataUpdateDto.data, dataUpdateDto.timestamp);
	}

}
