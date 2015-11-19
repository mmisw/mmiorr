package org.mmisw.ont;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.sparql.SparqlDispatcher;


/**
 * Entity URI resolver using SPARQL.
 * This is to be used when the "uri" parameter is passed.
 * However (see {@link OntServlet}), when that uri corresponds to an stored ontology,
 * then that is dispatched there, not here.
 *
 * TODO rename this class to EntityUriDispatcher.
 *
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 *
 * <p>
 * Thread-safety: This class may not be strictly thread-safe, but it is "effectively thread-safe"
 * in conjunction with {@link OntServlet} and other callers.
 *
 * @author Carlos Rueda
 * @version $Id$
 */
public class UriDispatcher {

	/** SELECT Query template to obtain all properties associated with an entity */
	private static final String PROPS_SELECT_QUERY_TEMPLATE =
		"SELECT DISTINCT ?property ?value " +
		"WHERE { <{E}> ?property ?value . } " +
		"ORDER BY ?property"
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
	 * Dispatches the given uri, which is assumed to be for a term or entity, not for a whole ontology.
	 * @param request
	 * @param response
	 * @param entityUri
	 * @return true iff the uri was dispatched here.
	 * @throws ServletException
	 * @throws IOException
	 */
	boolean dispatchEntityUri(HttpServletRequest request, Map<String, String[]> params, HttpServletResponse response,
			String entityUri, String outFormat
	)
	throws ServletException, IOException {

//		String entityUri = Util.getParam(request, "uri", "");
//		if ( entityUri.length() == 0 ) {
//			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing uri parameter");
//			return;
//		}

		outFormat = outFormat == null ? "owl" : outFormat;

		if ( log.isDebugEnabled() ) {
			log.debug("dispatchEntityUri: [" +entityUri+ "] format=" +outFormat);
		}

		if ( outFormat.equalsIgnoreCase("html") || outFormat.equalsIgnoreCase("csv") ) {
			return _dispatchUriHtml(request, params, response, entityUri, outFormat);
		}
		else if ( outFormat.equalsIgnoreCase("owl")
			 ||   outFormat.equalsIgnoreCase("rdf")
			 ||   outFormat.equalsIgnoreCase("n3")
			 ||   outFormat.equalsIgnoreCase("json")
			 ||   outFormat.equalsIgnoreCase("nt")
		) {
			return _dispatchUriOntologyFormat(request, params, response, entityUri, outFormat);
		}
		else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "output format not recognized: " +outFormat);
			return true;
		}
	}


	/**
	 * Replaces spaces with underscores.
	 */
	private String fixUriForQuery(String uri) {
		if (log.isDebugEnabled() && uri.matches(".*\\s.*")) {
			log.debug("fixing uri for SPARQL query because it contains spaces: '" + uri + "'");
		}
		return uri.replaceAll("\\s", "_");
	}

	/**
	 * Dispatches with a CONSTRUCT query.
	 * @return true iff dispatch completed here.
	 */
	private boolean _dispatchUriOntologyFormat(HttpServletRequest request, Map<String, String[]> params, HttpServletResponse response,
		String entityUri, String outFormat)
	throws IOException, ServletException {

		String query = PROPS_CONSTRUCT_QUERY_TEMPLATE.replace("{E}", fixUriForQuery(entityUri));

		// note, pass null so the caller can continue the dispatch if the query gets empty result
		String requestedEntity = null;
		return sparqlDispatcher.execute(request, params, response, query, requestedEntity, outFormat);
	}


	/**
	 * Dispatches with a SELECT query.
	 * @return true iff dispatch completed here.
	 */
	private boolean _dispatchUriHtml(HttpServletRequest request, Map<String, String[]> params, HttpServletResponse response,
		String entityUri,
		String outFormat
	)
	throws ServletException, IOException {

		String query = PROPS_SELECT_QUERY_TEMPLATE.replace("{E}", fixUriForQuery(entityUri));

		// note, pass null so the caller can continue the dispatch if the query gets empty result
		String requestedEntity = entityUri;
		return sparqlDispatcher.execute(request, params, response, query, requestedEntity, outFormat);
	}
}
