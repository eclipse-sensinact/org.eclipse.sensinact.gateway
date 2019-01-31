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
package org.eclipse.sensinact.gateway.sthbnd.inovallee.http;

public class Response {

	private final int httpCode;
	private final String payload;

	public Response(int httpCode, String payload) {
		super();
		this.httpCode = httpCode;
		this.payload = payload;
	}

	public int getHttpCode() {
		return httpCode;
	}
	
	public boolean isHttp2XX() {
		return httpCode >= 200 && httpCode <= 299;
	}
	
	public String getPayload() {
		return payload;
	}
	
	@Override
	public String toString() {
		return "Response [httpCode=" + httpCode + ", payload=" + payload + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response other = (Response) obj;
		if (httpCode != other.httpCode)
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + httpCode;
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		return result;
	}
}
