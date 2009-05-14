package org.mmisw.iserver.core;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.RelationInfo;

/**
 * Interface to get info from the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface IServer {
	
	/**
	 * Gets basic application info.
	 */
	AppInfo getAppInfo();
	
	/**
	 * Gets the vocabularies.
	 * Only the basic information about each ontology is obtained.
	 * Call {@link #getEntities(OntologyInfo)} to obtain the entities of an ontology.
	 */
	List<OntologyInfo> getAllOntologies();
	
	
	/**
	 * Obtains the entities of an ontology.
	 * @param ontologyInfo
	 * @return ontologyInfo
	 */
	OntologyInfo getEntities(OntologyInfo ontologyInfo);

	/**
	 * Gets the default list of RelationInfo's.
	 */
	List<RelationInfo> getRelationInfos();
	
	
	// TODO Not used; the scheme for the mappings still to be determined
	String performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms);
}
