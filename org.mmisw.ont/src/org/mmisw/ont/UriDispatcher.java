package org.mmisw.ont;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.sparql.SparqlDispatcher;
import org.mmisw.ont.util.Util;


/**
 * Entity URI resolver using SPARQL.
 * This is to be used when the "uri" parameter is passed.
 * However (see {@link OntServlet}), when that uri corresponds to an stored ontology,
 * then that is dispatched there, not here.
 * 
 * TODO rename this class to EntityUriDispatcher.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class UriDispatcher {
	
	/** SELECT Query template to obtain all properties associated with an entity */
	private static final String PROPS_SELECT_QUERY_TEMPLATE =
		"SELECT ?prop ?value " +
		"WHERE { <{E}> ?prop ?value . }"
	;
	
	
	/** CONSTRUCT Query template to obtain all properties associated with an entity */
	private static final String PROPS_CONSTRUCT_QUERY_TEMPLATE =
		"CONSTRUCT { <{E}> ?prop ?value }" +
		"    WHERE { <{E}> ?prop ?value . }"
	;
	
	
	private final Log log = LogFactory.getLog(UriDispatcher.class);
	
	private final SparqlDispatcher sparqlDispatcher;
	
	UriDispatcher(SparqlDispatcher sparqlDispatcher) {
		this.sparqlDispatcher = sparqlDispatcher;
	}


	
	/**
	 * Dispatches the URI indicated with the "uri" parameter in the request.
	 * The URI is assumed to be for a term or entity, not for a whole ontology.
	 */
	void dispatchEntityUri(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		String entityUri = Util.getParam(request, "uri", "");
		if ( entityUri.length() == 0 ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing uri parameter");
			return;
		}
		
		String outFormat = Util.getParam(request, "form", "owl");
		
		if ( log.isDebugEnabled() ) {
			log.debug("dispatchUri: [" +entityUri+ "] format=" +outFormat);
		}
		
		if ( outFormat.equalsIgnoreCase("html") || outFormat.equalsIgnoreCase("csv") ) {
			_dispatchUriHtml(request, response, entityUri);
		}
		else if ( outFormat.equalsIgnoreCase("owl")
			 ||   outFormat.equalsIgnoreCase("rdf")	 
			 ||   outFormat.equalsIgnoreCase("n3")	 
			 ||   outFormat.equalsIgnoreCase("json")	 
			 ||   outFormat.equalsIgnoreCase("nt")	 
		) {
			_dispatchUriOntologyFormat(request, response, entityUri, outFormat);
		}
		else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "output format not recognized: " +outFormat);
			return;
		}
	}


	/**
	 * Dispatches with a CONSTRUCT query.
	 */
	private void _dispatchUriOntologyFormat(HttpServletRequest request, HttpServletResponse response, 
		String entityUri, String outFormat) 
	throws IOException, ServletException {
		
		String query = PROPS_CONSTRUCT_QUERY_TEMPLATE.replaceAll("\\{E\\}", entityUri);
		
		// note, we pass the entityUri such that, if the result es empty, then 404 is returned
		sparqlDispatcher.execute(request, response, query, entityUri);
	}


	/**
	 * Dispatches with a SELECT query.
	 */
	private void _dispatchUriHtml(HttpServletRequest request, HttpServletResponse response, 
		String entityUri) 
	throws ServletException, IOException {
		
		String query = PROPS_SELECT_QUERY_TEMPLATE.replaceAll("\\{E\\}", entityUri);
		
		// note, we pass the entityUri such that, if the result es empty, then 404 is returned
		sparqlDispatcher.execute(request, response, query, entityUri);
	}
}
