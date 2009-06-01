package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;

/**
 * Basic Info about an ontology.
 * 
 * @author Carlos Rueda
 */
public class BasicOntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String error = null;
	
	private String authority;

	private String shortName;

	
	/** aquaportal ontology ID used, if not null, to create a new version */
	private String ontologyId = null;
	private String ontologyUserId = null;


	
	public BasicOntologyInfo() {
	}
	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
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

	/** @returns the aquaportal ontology ID used, if not null, to create a new version */
	public String getOntologyId() {
		return ontologyId;
	}

	/** @returns the aquaportal userId of the ontology ID used to create a new version */
	public String getOntologyUserId() {
		return ontologyUserId;
	}


	/** sets the aquaportal ontology ID used, if not null, to create a new version */
	public void setOntologyId(String ontologyId, String ontologyUserId) {
		this.ontologyId = ontologyId;
		this.ontologyUserId = ontologyUserId;
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
	 * @param ontologyId the ontologyId to set
	 */
	public void setOntologyId(String ontologyId) {
		this.ontologyId = ontologyId;
	}

	/**
	 * @param ontologyUserId the ontologyUserId to set
	 */
	public void setOntologyUserId(String ontologyUserId) {
		this.ontologyUserId = ontologyUserId;
	}



}
