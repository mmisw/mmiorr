package org.mmisw.vine.gwt.client.rpc;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;

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
	 * Call {@link #getEntities(RegisteredOntologyInfo)} to obtain the entities of an ontology.
	 */
	List<RegisteredOntologyInfo> getAllOntologies();
	
	
	/**
	 * Obtains the entities of an ontology.
	 * @param ontologyInfo
	 * @return ontologyInfo
	 */
	RegisteredOntologyInfo getEntities(RegisteredOntologyInfo ontologyInfo);

	/**
	 * Gets the default list of RelationInfo's.
	 */
	List<RelationInfo> getRelationInfos();
	
	
	// TODO Not used; the scheme for the mappings still to be determined
	String performMapping(List<String> leftTerms, int relationCode, List<String> rightTerms);
}
