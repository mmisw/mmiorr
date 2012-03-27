package org.mmisw.orrclient.core.util;

import java.util.Map;

import org.mmisw.ont.client.IOntClient;
import org.mmisw.orrclient.gwt.client.rpc.SparqlQueryInfo;

import com.hp.hpl.jena.ontology.OntModel;


/**
 * Utilities based on the "Ont" URI resolution service.
 * 
 * <p>
 * (Note: The original implementation was moved to the OntClient library. Now this class
 * serves as a proxy to avoid many changes elsewhere in this module for the moment.)
 * 
 * @author Carlos Rueda
 */
public class OntServiceUtil {
	private OntServiceUtil() {}
	
	private static IOntClient _ontClient;

	/**
	 * Called by OrrClientImpl to indicate the OntClient library interface. 
	 */
	public static void setOntClient(IOntClient ontClient) {
		OntServiceUtil._ontClient = ontClient;
	}

	public static String resolveOntologyUri(String uriModel, String version, String... acceptEntries) throws Exception {
		return _ontClient.resolveOntologyUri(uriModel, version, acceptEntries);
	}

	public static boolean isRegisteredOntologyUri(String uriModel) throws Exception {
		return _ontClient.isRegisteredOntologyUri(uriModel);
	}
	
	public static OntModel retrieveModel(String uriModel, String version) throws Exception {
		return _ontClient.retrieveModel(uriModel, version);
	}
	
	public static boolean isOntResolvableUri(String uri) {
		return _ontClient.isOntResolvableUri(uri);
	}
	
	public static String runSparqlQuery(String query, String format, String... acceptEntries) throws Exception {
		return _ontClient.runSparqlQuery(query, format, acceptEntries);
	}

	public static String runSparqlQuery(SparqlQueryInfo sqi) throws Exception {
		return _ontClient.runSparqlQuery(sqi.getEndPoint(), sqi.getQuery(), sqi.getFormat(), sqi.getAcceptEntries());
	}

	public static boolean loadOntologyInGraph(String uriModel, String graphId) throws Exception {
		return _ontClient.loadOntologyInGraph(uriModel, graphId);
	}
	
	public static Map<String,String> getUserInfo(String username) throws Exception {
		return _ontClient.getUserInfo(username);
	}
	
	public static String getUsersRdf() throws Exception {
		return _ontClient.getUsersRdf();
	}

	public static boolean unregisterOntology(String ontUri, String version) throws Exception {
		return _ontClient.unregisterOntology(ontUri, version);
	}

}

