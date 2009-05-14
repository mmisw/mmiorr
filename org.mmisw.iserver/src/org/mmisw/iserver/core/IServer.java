package org.mmisw.iserver.core;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;

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

}
