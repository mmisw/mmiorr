package org.mmisw.orrportal.gwt.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info about data associated with an ontology (in constrast to metadata).
 * 
 * @author Carlos Rueda
 */
public class DataResult extends BaseResult implements IsSerializable {

	private String csv = null;
	
	private OntologyInfoPre ontologyInfoPre;


	public DataResult() {
	}

	
	public OntologyInfoPre getOntologyInfo() {
		return ontologyInfoPre;
	}

	public void setOntologyInfo(OntologyInfoPre ontologyInfoPre) {
		this.ontologyInfoPre = ontologyInfoPre;
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
