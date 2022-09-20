package org.eclipse.sensinact.prototype.impl;

import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.dto.impl.MetadataUpdateDto;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class SetMetadataCommand extends AbstractSensinactCommand<Void> {
	
	private final MetadataUpdateDto metadataUpdateDto;
	
	public SetMetadataCommand(MetadataUpdateDto metadataUpdateDto) {
		this.metadataUpdateDto = metadataUpdateDto;
	}

	@Override
	protected Promise<Void> call(SensinactModel model, PromiseFactory promiseFactory) {
		
		//TODO set the metadata in the model
		
		return promiseFactory.resolved(null);
	}

}
