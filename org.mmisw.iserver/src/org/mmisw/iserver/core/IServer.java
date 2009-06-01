package org.mmisw.iserver.core;

import java.util.List;

import org.mmisw.iserver.gwt.client.rpc.AppInfo;
import org.mmisw.iserver.gwt.client.rpc.BasicOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateVocabularyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.UploadOntologyResult;

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
	 * Gets the list of entities associated with the given ontology. 
	 * @param ontologyUri URI of the desired ontology.
	 * @return list of entities
	 */
	public List<EntityInfo> getEntities(String ontologyUri);
	
	
	/**
	 * Gets all (latest versions of the) ontologies in the registry.
	 * 
	 * <p>
	 * Note that the all corresponding URIs are in versioned form.
	 * 
	 * @param includePriorVersions true to include the prior versions for each element
	 * @return
	 * @throws Exception
	 */
	List<OntologyInfo> getAllOntologies(boolean includePriorVersions) throws Exception;
	
	
	/**
	 * Obtains the entities of an ontology.
	 * @param ontologyInfo
	 * @return ontologyInfo
	 */
	OntologyInfo getEntities(OntologyInfo ontologyInfo);
	
	
	
	
	public MetadataBaseInfo getMetadataBaseInfo(boolean includeVersion, 
			String resourceTypeClassUri, String authorityClassUri);
	
	
	
	/**
	 * Gets both the metadata and the entities.
	 * 
	 * @param ontologyInfo
	 * @return
	 */
	public OntologyInfo getOntologyContents(OntologyInfo ontologyInfo);
	
	
	
	public CreateOntologyResult createVocabulary(BasicOntologyInfo basicOntologyInfo, CreateVocabularyInfo createOntologyInfo) ;

	
	public UploadOntologyResult uploadOntology(CreateOntologyResult createOntologyResult, LoginResult loginResult) ;
}
