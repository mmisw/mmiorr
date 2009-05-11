package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about data associated with an ontology (in constrast to metadata).
 * 
 * @author Carlos Rueda
 */
public class DataResult extends BaseResult implements IsSerializable {

	private String csv = null;
	
	private OntologyInfo ontologyInfo;


	public DataResult() {
	}

	
	public OntologyInfo getOntologyInfo() {
		return ontologyInfo;
	}

	public void setOntologyInfo(OntologyInfo ontologyInfo) {
		this.ontologyInfo = ontologyInfo;
	}

	public String getCsv() {
		return csv;
	}

	public void setCsv(String csv) {
		this.csv = csv;
	}

	public String toString() {
		return "DataResult{csv=" +csv+ "}";
	}


}
