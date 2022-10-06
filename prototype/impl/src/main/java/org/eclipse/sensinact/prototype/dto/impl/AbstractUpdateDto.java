package org.eclipse.sensinact.prototype.dto.impl;

import java.time.Instant;

public abstract class AbstractUpdateDto {

	public String model;
	
	public String provider;
	
	public String service;
	
	public String resource;
	
	public Instant timestamp;
}
