package org.mmisw.iserver.core.util.ontinfo;

import org.mmisw.iserver.gwt.client.rpc.BaseOntologyInfo;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * Provides info about an ontology model.
 * 
 * Refactoring interface.
 * 
 * @author Carlos Rueda
 */
interface IOntInfo {

	/**
	 * Populates the list of entities associated with the given ontology. 
	 * @param baseOntologyInfo
	 * @return the given argument baseOntologyInfo
	 * @throws Exception 
	 */
	public BaseOntologyInfo getEntities(BaseOntologyInfo baseOntologyInfo, OntModel ontModel) 
	throws Exception;
	
}