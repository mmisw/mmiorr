package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Carlos Rueda
 */
public class CreateOntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String authority;
	private String shortName;
	

	private Map<String,String> metadataValues;
	
	
	public CreateOntologyInfo() {
		
	}
	
	
	/**
	 * @return the authority
	 */
	public String getAuthority() {
		return authority;
	}


	/**
	 * @param authority the authority to set
	 */
	public void setAuthority(String authority) {
		this.authority = authority;
	}


	/**
	 * @return the shortName
	 */
	public String getShortName() {
		return shortName;
	}


	/**
	 * @param shortName the shortName to set
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return the metadataValues
	 */
	public Map<String, String> getMetadataValues() {
		return metadataValues;
	}

	/**
	 * @param metadataValues the metadataValues to set
	 */
	public void setMetadataValues(Map<String, String> metadataValues) {
		this.metadataValues = metadataValues;
	}

	
}
