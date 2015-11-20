package org.mmisw.ont;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

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

	/**
	 * Simple regex to verify that a URI is an IRI for purposes of its direct use in SPARQL query.
	 * See http://www.w3.org/TR/rdf-sparql-query/#QSynIRI
	 * Note: this pattern is only about the characters that are allowed, not about the structure of the IRI.
	 * Also, for simplicity, it excludes any space as determined by java via the "\\s" regex; this
	 * may not exactly match the specification but should be good enough for our purposes.
	 */
	static final Pattern goodIriCharactersPattern = Pattern.compile("[^\\s<>\"{}|\\\\^`]*");

	/** Alternate SELECT Query template to obtain all properties associated with an entity
	 * whose URI cannot be used directly per SPARQL restrictions */
	private static final String PROPS_SELECT_QUERY_TEMPLATE_ALTERNATE =
		"SELECT ?property ?value " +
		"WHERE { ?s ?property ?value FILTER (str(?s) = \"{E}\") } " +
		"ORDER BY ?property"
	;

	/** Alternate CONSTRUCT Query template to obtain all properties associated with an entity
	 * whose URI cannot be used directly per SPARQL restrictions */
	private static final String PROPS_CONSTRUCT_QUERY_TEMPLATE_ALTERNATE =
		"CONSTRUCT { ?s ?property ?value } " +
		"    WHERE { ?s ?property ?value FILTER (str(?s) = \"{E}\") }"
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
	 * Dispatches with a CONSTRUCT query.
	 * @return true iff dispatch completed here.
	 */
	private boolean _dispatchUriOntologyFormat(HttpServletRequest request, Map<String, String[]> params, HttpServletResponse response,
		String entityUri, String outFormat)
	throws IOException, ServletException {

		String template = goodIriCharactersPattern.matcher(entityUri).matches()
				? PROPS_CONSTRUCT_QUERY_TEMPLATE
				: PROPS_CONSTRUCT_QUERY_TEMPLATE_ALTERNATE;
		String query = template.replace("{E}", entityUri);

		// note, pass null so the caller can continue the dispatch if the query gets empty result
		return sparqlDispatcher.execute(request, params, response, query, null, outFormat);
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

		String template = goodIriCharactersPattern.matcher(entityUri).matches()
				? PROPS_SELECT_QUERY_TEMPLATE
				: PROPS_SELECT_QUERY_TEMPLATE_ALTERNATE;
		String query = template.replace("{E}", entityUri);

		// note, pass the non-null entityUri as requestedEntity so a 404 is generated if the query's result is empty:
		return sparqlDispatcher.execute(request, params, response, query, entityUri, outFormat);
	}
}
