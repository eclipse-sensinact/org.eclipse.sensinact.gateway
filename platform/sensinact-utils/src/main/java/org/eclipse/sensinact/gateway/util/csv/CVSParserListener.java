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