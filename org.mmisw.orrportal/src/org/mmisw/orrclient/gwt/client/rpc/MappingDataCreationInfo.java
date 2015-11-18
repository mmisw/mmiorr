package org.mmisw.orrclient.gwt.client.rpc;

import java.util.List;
import java.util.Set;

import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;


/**
 * Data Info for the creation of a mapping ontology from the VINE.
 *
 * @author Carlos Rueda
 */
public class MappingDataCreationInfo extends DataCreationInfo {
	private static final long serialVersionUID = 1L;


	private List<Mapping> mappings;

	/** URIs of mapped ontologies */
	private Set<String> uris;


	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}


	public List<Mapping> getMappings() {
		return mappings;
	}

	public void setUris(Set<String> namespaces) {
		this.uris = namespaces;
	}

	public Set<String> getUris() {
		return uris;
	}

}
