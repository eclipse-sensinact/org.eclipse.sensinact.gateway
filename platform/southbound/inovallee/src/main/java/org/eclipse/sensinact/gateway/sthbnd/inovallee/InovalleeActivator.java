/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.inovallee;


import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.generic.GenericActivator;
import org.eclipse.sensinact.gateway.generic.model.Resource;
import org.eclipse.sensinact.gateway.generic.model.Tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sensiNact bundle activator
 */
public class InovalleeActivator extends GenericActivator {

	private static final Logger LOG = LoggerFactory.getLogger(InovalleeActivator.class);

	@Override
	public void doStart() throws Exception {
		super.doStart();
		Tree tree = new Fetcher().fetch();
		List<InovalleePacket> packets = treeToPackets(tree);
		processPackets(packets);
	}
	
	private List<InovalleePacket> treeToPackets(Tree tree) {
		return tree.getResources().stream().map(this::toPacket).collect(Collectors.toList());
	}

	private InovalleePacket toPacket(Resource r) {
		return new InovalleePacket(r.getProvider().getId(), r.getService().getId(), r.getId(), r.getValue());
	}

	@Override
	public InovalleeProtocolStackEndpoint getEndPoint() {
		return new InovalleeProtocolStackEndpoint(mediator);
	}

	public Class getPacketClass() {
		return InovalleePacket.class;
	}
}
