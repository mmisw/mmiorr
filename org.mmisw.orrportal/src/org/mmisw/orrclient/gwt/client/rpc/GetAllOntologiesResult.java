package org.mmisw.orrclient.gwt.client.rpc;

import java.util.List;

/**
 * Info about the result of a getAllOntologies operation.
 * 
 * @author Carlos Rueda
 */
public class GetAllOntologiesResult extends BaseResult {
	private static final long serialVersionUID = 1L;

	/** the list of ontologies */
	private List<RegisteredOntologyInfo> ontologyList;
	
	// no-arg ctor for serialization
	public GetAllOntologiesResult() {
	}
	
	public List<RegisteredOntologyInfo> getOntologyList() {
		return ontologyList;
	}

	public void setOntologyList(List<RegisteredOntologyInfo> list) {
		this.ontologyList = list;
	}

}
