package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;

/**
 * Info about an individual.
 * 
 * @author Carlos Rueda
 */
public class IndividualInfo extends EntityInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// my type
	private String classUri;
	
	public IndividualInfo() {
		super();
	}

	/**
	 * @return the classUri
	 */
	public String getClassUri() {
		return classUri;
	}

	/**
	 * @param classUri the classUri to set
	 */
	public void setClassUri(String classUri) {
		this.classUri = classUri;
	}

	
}
