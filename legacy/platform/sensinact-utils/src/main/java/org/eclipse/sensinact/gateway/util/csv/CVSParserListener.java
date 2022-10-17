/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.csv;

import java.util.concurrent.CountDownLatch;

public class CVSParserListener implements CVSParserCallback{
	
	private CVSParserEvent event;
	private CountDownLatch countDown;
	
	public CVSParserListener() {
		this.countDown = new CountDownLatch(1);
	}
	
	@Override
	public synchronized void handle(CVSParserEvent event) {
		this.event = event;
		await();
	}
	
	public CVSParserEvent getEvent() {
		return this.event;
	}
	
	public void countDown() {
		this.event = null;
		this.countDown.countDown();		
	}

	public void await() {
		try {
			this.countDown.await();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
		this.countDown = new CountDownLatch(1);
	}
}