package org.mmisw.ontmd.gwt.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OntMdService extends RemoteService {

	/**
	 * Gets the base information (metadata attribute definitions).
	 */
	BaseInfo getBaseInfo(Map<String, String> params);
	
	/**
	 * Authenticates a user.
	 */
	LoginResult login(String userName, String userPassword);
	
	
	/**
	 * Gets ontology info from an ontology that can be resolved by the Ont service
	 * (ie., from the MMI registry).
	 */
	OntologyInfo getOntologyInfoFromRegistry(String ontologyUri);

	/**
	 * Gets ontology info from a pre-loaded file.
	 */
	OntologyInfo getOntologyInfoFromPreLoaded(String uploadResults);

	/**
	 * Gets ontology info from a file located in the server.
	 * The main "client" for this service is Voc2Rdf
	 */
	OntologyInfo getOntologyInfoFromFileOnServer(String fullPath);

	/**
	 * Reviews the pre-loaded model with the associated new values.
	 */
	ReviewResult review(OntologyInfo ontologyInfo, LoginResult loginResult);

	/**
	 * Uploads a reviewed model to the MMI Registry.
	 */
	UploadResult upload(ReviewResult reviewResult, LoginResult loginResult);
	
}
