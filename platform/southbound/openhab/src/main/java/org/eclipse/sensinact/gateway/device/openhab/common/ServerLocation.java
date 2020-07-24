package org.eclipse.sensinact.gateway.device.openhab.common;

public class ServerLocation {
	
	private static final String DEFAULT_SCHEME = "http";
	private final String host;
	private final int port;
	private final String scheme;

	public ServerLocation(String host, int port) {
		this(DEFAULT_SCHEME,host,port);
	}

	public ServerLocation(String scheme, String host, int port) {
		this.host = host;
		this.port = port;
		this.scheme = scheme;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getScheme() {
		if(this.scheme == null) {
			return DEFAULT_SCHEME;
		}
		return this.scheme;
	}	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerLocation other = (ServerLocation) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "OpenHabServer [host=" + host + ", port=" + port + "]";
	}
}
