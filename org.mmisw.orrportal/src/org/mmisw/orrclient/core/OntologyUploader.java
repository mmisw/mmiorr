package org.mmisw.orrclient.core;

import java.util.Map;

//import org.mmisw.ont.client.OntUploader;
//import org.mmisw.ont.client.SignInResult;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;

/** 
 * A helper class to upload ontologies into the repository.
 *
 * OBSOLETE CLASS
 */
class OntologyUploader {
	
//	private SignInResult signInResult;
//	private OntUploader ontUploader;
	
	/**
	 * Constructor.
	 * @param uri
	 * @param fileName
	 * @param RDF Contents of the ontology
	 * @param loginResult
	 * @param ontologyId Aquaportal ontology ID when creating a new version.
	 * 
	 * @param values   Used to fill in some of the fields in the aquaportal request
	 * @throws Exception
	 */
	OntologyUploader(String uri, String fileName, String RDF, 
			LoginResult loginResult,
			String ontologyId, String ontologyUserId,
			Map<String, String> values
	) throws Exception {

        throw new RuntimeException("OBSOLETE CLASS");
		
/*
		signInResult = new SignInResult();
		signInResult.setSessionId(loginResult.getSessionId());
		signInResult.setUserId(loginResult.getUserId());
		signInResult.setUserName(loginResult.getUserName());
		signInResult.setUserRole(loginResult.getUserRole());

		ontUploader = new OntUploader(uri, fileName, RDF, signInResult, ontologyId, ontologyUserId, values) ;
*/
	}
	
	
	/**
	 * Executes the POST operation to upload the ontology.
	 * 
	 * @return The message in the response from the POST operation, prefixed with "OK:" if
	 *         the result was successful; otherwise, the description of the error 
	 *         prefixed with "ERROR:"
	 *         
	 * @throws Exception
	 */
	String create()	throws Exception {
        throw new RuntimeException("OBSOLETE CLASS");
//		return ontUploader.create();
	}
}
