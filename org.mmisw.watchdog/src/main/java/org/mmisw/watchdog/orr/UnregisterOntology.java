package org.mmisw.watchdog.orr;

import org.mmisw.orrclient.IOrrClient;
import org.mmisw.orrclient.OrrClientConfiguration;
import org.mmisw.orrclient.gwt.client.rpc.LoginResult;
import org.mmisw.orrclient.gwt.client.rpc.RegisteredOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.UnregisterOntologyResult;
import org.mmisw.watchdog.util.WdConstants;

/**
 * A program demonstrating the direct unregistration of an ontology 
 * in the MMI Ontology Registry and Repository.
 * 
 * @author Carlos Rueda
 */
public class UnregisterOntology {
	
	private static final String className = UnregisterOntology.class.getSimpleName();
	private static final String DEFAULT_BIOPORTAL_REST_URL = null;

	
	private static final String PREVIEW_DIRECTORY  = "/tmp/orrclient/";
	private static final String authorityClassUri = "http://mmisw.org/ont/mmi/authority/Authority";
	private static final String resourceTypeClassUri = "http://mmisw.org/ont/mmi/resourcetype/ResourceType";


	/** Never returns */
	private static void _usage(String msg) {
		if ( msg == null ) {
			System.out.println(
					"USAGE: " +className+ " [options] --ontology <uri>\n" +
					"  options: (default value in parenthesis)\n" +
					"    --ontServiceUrl <url> (" +WdConstants.ORR_DEFAULT_USERNAME+ ")\n" +
					"    --bioportalRestUrl <url> (" +DEFAULT_BIOPORTAL_REST_URL+ ")\n" +
					"    --username <username> (" +WdConstants.ORR_DEFAULT_USERNAME+ ")\n" +
					"    --password <password> \n" +
					"   for unregistration:\n" +
					"    --ontology <uri>      \n" +
					"");
			System.exit(0);
		}
		else {
			System.err.println("Error: " +msg);
			System.err.println("Try " +className+ " --help\n");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		if ( args.length == 0 || args[0].matches(".*help") ) {
			_usage(null);
		}

		String ontServiceUrl = WdConstants.DEFAULT_ONT_SERVICE_URL;
		String bioportalRestUrl = DEFAULT_BIOPORTAL_REST_URL;
		String orrUsername = WdConstants.ORR_DEFAULT_USERNAME;
		String orrPassword = null;
		String ontologyUri = null;

	
		int arg = 0;
		for ( ; arg < args.length && args[arg].startsWith("--"); arg++ ) {
			if ( args[arg].equals("--ontServiceUrl") ) {
				ontServiceUrl = args[++arg]; 
			}
			else if ( args[arg].equals("--bioportalRestUrl") ) {
				bioportalRestUrl = args[++arg]; 
			}
			else if ( args[arg].equals("--username") ) {
				orrUsername = args[++arg]; 
			}
			else if ( args[arg].equals("--password") ) {
				orrPassword = args[++arg]; 
			}
			else if ( args[arg].equals("--ontologyUri") ) {
				ontologyUri = args[++arg]; 
			}
		}
		if ( arg < args.length ) {
			String uargs = "";
			for ( ; arg< args.length; arg++ ) {
				uargs += args[arg] + " ";
			}
			_usage("Unexpected arguments: " +uargs);
		}		
		
		
		if ( orrPassword == null ) {
			_usage("Password missing");
			return;
		}
		if ( ontologyUri == null ) {
			_usage("ontologyUri missing");
			return;
		}
		
		
		UnregisterOntologyResult result = unregister(
				ontServiceUrl, bioportalRestUrl, ontologyUri, orrUsername, orrPassword);
		
		if ( result.getError() != null ) {
			System.out.println("unregister: error: " +result.getError());
			return;
		}
		
		System.out.println("unregister: OK: " +result.getUri());
		
	}
	
	
	
	public static UnregisterOntologyResult unregister(
			String ontServiceUrl, 
			String bioportalRestUrl, String ontologyUri, String username, String password
	) throws Exception {
		
		
		OrrClientConfiguration config = new  OrrClientConfiguration();
		config.setOntServiceUrl(ontServiceUrl);
		config.setPreviewDirectory(PREVIEW_DIRECTORY);
		IOrrClient orrClient = IOrrClient.Manager.init(config);
		orrClient.getMetadataBaseInfo(false, resourceTypeClassUri, authorityClassUri);
		
		
		RegisteredOntologyInfo registeredOntologyInfo = orrClient.getOntologyInfo(ontologyUri);
		if ( registeredOntologyInfo.getError() != null ) {
			System.out.println("getOntologyInfo: " +registeredOntologyInfo.getError());
			return null;
		}
		
		System.out.println("getOntologyInfo: " +registeredOntologyInfo.getDisplayLabel());
		
		LoginResult loginResult = orrClient.authenticateUser(username, password);
		if ( loginResult.getError() != null ) {
			System.out.println("authenticateUser: " +loginResult.getError());
			return null;
		}
		System.out.println("authenticateUser: sessionId=" +loginResult.getSessionId());
		
		
		return orrClient.unregisterOntology(loginResult, registeredOntologyInfo);
	}

}
