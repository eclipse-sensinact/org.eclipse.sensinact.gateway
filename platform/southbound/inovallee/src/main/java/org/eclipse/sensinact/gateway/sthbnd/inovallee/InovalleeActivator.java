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

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.generic.ExtModelConfigurationBuilder;
import org.eclipse.sensinact.gateway.generic.GenericActivator;
import org.eclipse.sensinact.gateway.generic.model.Resource;
import org.eclipse.sensinact.gateway.generic.model.Tree;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.fetcher.EliorFetcher;
import org.eclipse.sensinact.gateway.sthbnd.inovallee.fetcher.MobilityFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sensiNact bundle activator
 */
public class InovalleeActivator extends GenericActivator {

	private static final Logger LOG = LoggerFactory.getLogger(InovalleeActivator.class);
	private Periodic periodic;
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		LOG.info("Thread created for " + InovalleeActivator.class.getName());
		periodic = new Periodic(new Runnable() {
			@Override
			public void run() {
				try {
					LOG.info("Periodic update triggered " + InovalleeActivator.class.getName());
					updateData();
				} catch (Exception e) {
					LOG.error("Error during periodic update : " + e.getMessage());
				}
			}
		}, 1, 60, SECONDS);
	}

	@Override
	protected ExtModelConfigurationBuilder configureBuilder(ExtModelConfigurationBuilder builder) {
		return builder.withResourceImplementationType(InoResourceImpl.class);
	}
	
	@Override
	public void doStop() {
		super.doStop();
		LOG.info("Thread killed for " + InovalleeActivator.class.getName());
		periodic.stop();
	}
	
	void updateData() throws Exception {
		List<InovalleePacket> eliorPackets = treeToPackets(new EliorFetcher().fetch());
		LOG.info("Processing " + eliorPackets.size() + " Elior packets");
		processPackets(eliorPackets);
		List<InovalleePacket> mobilityPackets = treeToPackets(new MobilityFetcher().fetch());
		LOG.info("Processing " + mobilityPackets.size() + " mobility packets");
		processPackets(mobilityPackets);
	}

	private List<InovalleePacket> treeToPackets(Tree tree) {
		List<InovalleePacket> list = new ArrayList<>();
		for (Resource r : tree.getResources())
			list.add(new InovalleePacket(r.getProvider().getId(), r.getService().getId(), r.getId(), r.getValue()));
		return list;
	}

	@Override
	public InovalleeProtocolStackEndpoint getEndPoint() {
		return new InovalleeProtocolStackEndpoint(mediator);
	}

	public Class<InovalleePacket> getPacketClass() {
		return InovalleePacket.class;
	}
}
