package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;

/**
 * A prop-value pair.
 * 
 * @author Carlos Rueda
 */
public class PropValue implements Serializable {
	private static final long serialVersionUID = 1L;

	private String propName;
	private String propUri;
	
	private String valueName;
	private String valueUri;
	
	
	// no-arg constructor
	public PropValue() {
	}
	
	
	public PropValue(String propName, String propUri, String valueName, String valueUri) {
		super();
		this.propName = propName;
		this.propUri = propUri;
		this.valueName = valueName;
		this.valueUri = valueUri;
	}


	public String getPropName() {
		return propName;
	}


	public void setPropName(String propName) {
		this.propName = propName;
	}


	public String getPropUri() {
		return propUri;
	}


	public void setPropUri(String propUri) {
		this.propUri = propUri;
	}


	public String getValueName() {
		return valueName;
	}


	public void setValueName(String valueName) {
		this.valueName = valueName;
	}


	public String getValueUri() {
		return valueUri;
	}


	public void setValueUri(String valueUri) {
		this.valueUri = valueUri;
	}


	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	

}
