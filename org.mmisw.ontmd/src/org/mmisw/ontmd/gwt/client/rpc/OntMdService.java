package org.mmisw.ontmd.gwt.client.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface to interact with the server.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public interface OntMdService extends RemoteService {

	BaseInfo getBaseInfo();
	
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
	 * Reviews the pre-loaded model with the associated new values.
	 */
	ReviewResult review(OntologyInfo ontologyInfo, LoginResult loginResult);

	/**
	 * Uploads a reviewed model to the MMI Registry.
	 */
	UploadResult upload(ReviewResult reviewResult, LoginResult loginResult);
	
}
