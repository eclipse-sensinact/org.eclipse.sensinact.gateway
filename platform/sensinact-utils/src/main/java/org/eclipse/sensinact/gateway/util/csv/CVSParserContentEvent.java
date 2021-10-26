package org.eclipse.sensinact.gateway.util.csv;

public class CVSParserContentEvent extends CVSParserEvent{
	
	private String valueType;

	public CVSParserContentEvent( int pos, String valueType, String value) {
		super(pos, value);
		this.valueType = valueType;
	}

	/**
	 * @return the valueType
	 */
	public String getValueType() {
		return valueType;
	}

	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

}
