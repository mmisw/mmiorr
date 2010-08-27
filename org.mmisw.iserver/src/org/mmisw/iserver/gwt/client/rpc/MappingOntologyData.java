package org.mmisw.iserver.gwt.client.rpc;

import java.util.List;
import java.util.Set;

import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;

/**
 * The data associated with a mapping ontology.
 * 
 * @author Carlos Rueda
 */
public class MappingOntologyData extends OntologyData {
	private static final long serialVersionUID = 1L;
	
	/**
	 * The list of relations associated with this mapping ontology.
	 */
	private List<RelationInfo> relInfos;
	
	/** 
	 * The list of mappings 
	 */
	private List<Mapping> mappings;
	
	/** 
	 * Associated namespaces, ie., the URIs of the ontologies from which terms are mapped.
	 */
	private Set<String> namespaces;
	
	public MappingOntologyData() {
	}

	public List<Mapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<Mapping> mappings) {
		this.mappings = mappings;
	}

	public void setNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

	public Set<String> getNamespaces() {
		return namespaces;
	}

	public List<RelationInfo> getRelationInfos() {
		return relInfos;
	}

	public void setRelationInfos(List<RelationInfo> relInfos) {
		this.relInfos = relInfos;
	}

}
