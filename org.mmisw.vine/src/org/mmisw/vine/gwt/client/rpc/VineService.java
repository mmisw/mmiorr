package org.mmisw.vine.gwt.client.rpc;

import java.util.List;

import org.mmisw.vine.gwt.client.rpc.AppInfo;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to get info from the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface VineService extends RemoteService {
	
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
