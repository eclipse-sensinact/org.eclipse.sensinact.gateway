/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
