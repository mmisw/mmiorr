package org.mmisw.iserver.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.util.AquaUtil;
import org.mmisw.iserver.core.util.OntServiceUtil;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.PrepareUsersOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;

/**
 * Functionality related with internal information (users, groups, permissions, issues)
 * 
 * <p>
 * Status: preliminary implementation
 * 
 * @author Carlos Rueda
 */
public class InternalManager {
	
	private static final Log log = LogFactory.getLog(InternalManager.class);
	
	
	/**
	 * Prepares the users instantiation ontology.
	 * @param loginResult Only the administrator can perform this operation.
	 * @param usersUri the URI of the ontology to be created/updated.
	 * @param result to return the result of the operation
	 * @throws Exception 
	 */
	static void prepareUsersOntology(LoginResult loginResult, 
			String usersUri, 
			RegisteredOntologyInfo usersOntology,
			PrepareUsersOntologyResult result
	) throws Exception {
		
		log.debug("prepareUsersOntology: usersUri: " +usersUri);
		
		String ontologyId = null;
		String ontologyUserId = null;
		
		if ( usersOntology != null ) {
			ontologyId = usersOntology.getOntologyId();
			ontologyUserId = usersOntology.getOntologyUserId();
		}
		
		
		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			result.setError("Only an administrator can perform this operation.");
			return;
		}
		
		String fileName = AquaUtil.getAquaportalFilename(usersUri);
		
		String rdf = OntServiceUtil.getUsersRdf();
		
		Map<String, String> newValues = new HashMap<String, String>();
		
		OntologyUploader createOnt = new OntologyUploader(usersUri, fileName, rdf, 
				loginResult,
				ontologyId, ontologyUserId,
				newValues
		);
		String res = createOnt.create();
		
		if ( res.startsWith("OK") ) {
			result.setUri(usersUri);
			result.setInfo(res);
			
			// TODO: indicate graph for the internal information.
			String graphId = usersUri;
			
			// request that the ontology be loaded in the desired graph:
			OntServiceUtil.loadOntologyInGraph(usersUri, graphId );
			
			log.info("prepareUsersOntology = " +result);
		}
		else {
			result.setError(res);
		}
	}

}
