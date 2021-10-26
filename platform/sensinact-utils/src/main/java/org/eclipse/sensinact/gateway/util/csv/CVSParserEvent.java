package org.eclipse.sensinact.gateway.util.csv;

public abstract class CVSParserEvent {
	
	private String value;
	private int pos;
	
	public CVSParserEvent(int pos, String value) {
		this.pos = pos;
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the pos
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param pos the pos to set
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

}
