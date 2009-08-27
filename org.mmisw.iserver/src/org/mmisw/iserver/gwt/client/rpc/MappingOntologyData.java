package org.mmisw.iserver.gwt.client.rpc;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;

/**
 * TODO
 * @author Carlos Rueda
 */
public class MappingOntologyData extends OntologyData {
	private static final long serialVersionUID = 1L;
	
	/** the data is the list of mappings */
	private List<Mapping> mappings;
	
	public MappingOntologyData() {
	}

	public List<Mapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}




}
