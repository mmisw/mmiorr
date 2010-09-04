package org.mmisw.iserver.gwt.client.rpc;

import java.util.List;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;


/**
 * Data Info for the creation of a mapping ontology from the VINE.
 * 
 * @author Carlos Rueda
 */
public class MappingDataCreationInfo extends DataCreationInfo {
	private static final long serialVersionUID = 1L;
	

	private List<Mapping> mappings;
	
	/** and associated namespaces */
	private Set<String> namespaces;
	
	
	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}


	public List<Mapping> getMappings() {
		return mappings;
	}
	
	public void setNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

	public Set<String> getNamespaces() {
		return namespaces;
	}
	
}
