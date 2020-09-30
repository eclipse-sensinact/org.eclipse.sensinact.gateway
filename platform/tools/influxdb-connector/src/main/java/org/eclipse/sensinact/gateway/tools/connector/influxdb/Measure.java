/*
 * Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.tools.connector.influxdb;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

@Measurement(name = "test")
public class Measure {

	@Column(name="path")
	private String path;
	
	@Column(name="value")
	private String value;

	@Column(name="time")
	private String time;
	
	public Measure() {}
	
	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
	
	public void setTime(String time) {
		this.time = time;
	}

	public String getTime() {
		return this.time;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return this.path;
	}
	
	@Override
	public String toString() {		
		return "{'path':'"+path+"','value':'"+value+"','time':'"+time+"'}";
	}

}