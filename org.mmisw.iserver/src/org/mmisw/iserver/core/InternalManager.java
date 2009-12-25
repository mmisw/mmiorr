package org.mmisw.iserver.core;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.util.AquaUtil;
import org.mmisw.iserver.core.util.OntServiceUtil;
import org.mmisw.iserver.gwt.client.rpc.LoginResult;
import org.mmisw.iserver.gwt.client.rpc.PrepareUsersOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.MmiUri;
import org.mmisw.ont.vocabulary.Omv;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
	 * @param server Used to obtain previous version, if any.
	 * @param loginResult Only the administrator can perform this operation.
	 * @param result to return the result of the operation
	 * @throws Exception 
	 */
	static void prepareUsersOntology(
			IServer server,
			LoginResult loginResult, 
			PrepareUsersOntologyResult result
	) throws Exception {
		
		log.debug("prepareUsersOntology called.");
		
		if ( loginResult == null || ! loginResult.isAdministrator() ) {
			result.setError("Only an administrator can perform this operation.");
			return;
		}
		
		// get the user RDF from the Ont service:
		String rdf = OntServiceUtil.getUsersRdf();
		
		// get the users RDF URI, assumed to be versioned:
		String generatedUsersUri = _getUsersUri(rdf);
		MmiUri mmiUri = new MmiUri(generatedUsersUri);
		String version = mmiUri.getVersion();
		
		// unversioned URI for purposes of geting possible prior version, set up auqportal filename, 
		// graphId, and report it in the result back to caller:
		final String unversUsersUri = mmiUri.copyWithVersion(null).getOntologyUri();
		
		// info from previous version if any:
		String ontologyId = null;
		String ontologyUserId = null;
		RegisteredOntologyInfo usersOntology = server.getOntologyInfo(unversUsersUri);
		if ( usersOntology != null ) {
			ontologyId = usersOntology.getOntologyId();
			ontologyUserId = usersOntology.getOntologyUserId();
		}
		
		// set some associated attributes for the registration:
		Map<String, String> newValues = _getValues(loginResult, version);
		String fileName = AquaUtil.getAquaportalFilename(unversUsersUri);
		
		// register:
		OntologyUploader createOnt = new OntologyUploader(
				generatedUsersUri, fileName, rdf, 
				loginResult,
				ontologyId, ontologyUserId,
				newValues
		);
		String res = createOnt.create();
		
		if ( res.startsWith("OK") ) {
			result.setUri(unversUsersUri);
			result.setInfo(res);
			
			// TODO: indicate graph for the internal information.
			// for now, only associting with graph of the same URI:
			String graphId = unversUsersUri;
			
			// request that the ontology be loaded in the desired graph:
			OntServiceUtil.loadOntologyInGraph(unversUsersUri, graphId);
			
			log.info("prepareUsersOntology = " +result);
		}
		else {
			result.setError(res);
		}
	}


	/** Obtains the URI of the associated RDF, which it's assumed to be *versioned*; see OntServlet in Ont module. */
	private static String _getUsersUri(String rdf) {
		final Model model = ModelFactory.createDefaultModel();
		model.read(new StringReader(rdf), null);
		String ns = model.getNsPrefixURI("");
		
		String uri = JenaUtil2.removeTrailingFragment(ns);
		return uri;
	}


	private static Map<String, String> _getValues(LoginResult loginResult, String version) {
		Map<String, String> values = new HashMap<String, String>();
		
		values.put(Omv.name.getURI(), "MMI ORR Users");
		values.put(Omv.hasCreator.getURI(), loginResult.getUserName());
		values.put(Omv.version.getURI(), version);
		
		return values;
	}

}
