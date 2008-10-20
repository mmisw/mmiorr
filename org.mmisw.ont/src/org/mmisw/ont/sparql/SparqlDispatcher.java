package org.mmisw.ont.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.OntGraph;
import org.mmisw.ont.util.Unfinished;
import org.mmisw.ont.util.Util;
import org.mmisw.ont.util.XSLTCreator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;



/**
 * Dispatcher of SPARQL queries.
 * 
 * @author Carlos Rueda
 */
@Unfinished(priority=Unfinished.Priority.MEDIUM)
public class SparqlDispatcher {
	
	/** The default query */
	private static final String SPARQL_EXAMPLE = "CONSTRUCT { ?s ?p ?o } where {?s ?p ?o. } LIMIT 20";
	
	private final Log log = LogFactory.getLog(SparqlDispatcher.class);
	
	// ontGraph.getModel() is used as the model to process queries
	private final OntGraph ontGraph;
	
	public SparqlDispatcher(OntGraph ontGraph) {
		this.ontGraph = ontGraph;
	}

	/** 
	 * Executes the query indicated as argument of the "sparql" parameter.
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String query = Util.getParam(request, "sparql", "");
		if ( query.length() == 0 ) {
			query = SPARQL_EXAMPLE;
		}
		String result = _getRDF(query);
		
		// convert to HTML?
		if ( Util.yes(request, "xslt") ) {
			String XSLT_RESOURCE = "rdf.xslt";
			InputStream xslt = getClass().getClassLoader().getResourceAsStream(XSLT_RESOURCE);
			if ( xslt != null ) {
				result = XSLTCreator.create(result, xslt);
			}
			else {
				result = "Cannot find resource: " + XSLT_RESOURCE;
			}
			response.setContentType("text/html");
		}
		
		// put stylesheet at beginning of the result?
		else if ( Util.yes(request, "xslti") ) {
			// what type? I've tried:
			//   type="text/xsl"
			//   type="text/xml"
			//   type="application/xslt+xml"
			// without success.
			//
			String type="application/xslt+xml";
			String xmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>\n";
			xmlHeader += "<?xml-stylesheet type=\"" +type+ "\" href=\"" +
							request.getContextPath()+ "/rdf.xslt" + "\"?>\n";
			
			result = xmlHeader + result;
			response.setContentType("Application/rdf+xml");
		}
		else {
			response.setContentType("Application/rdf+xml");
		}
		StringReader is = new StringReader(result);
		ServletOutputStream os = response.getOutputStream();
		IOUtils.copy(is, os);
		os.close();
	}

	private String _getRDF(String sparqlQuery) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("getRDF: query string = [" +sparqlQuery+ "]");
		}
		
		String result = Sparql.getRDF(ontGraph.getModel(), sparqlQuery);

		if ( log.isDebugEnabled() ) {
			log.debug("getRDF: result = [" +result+ "]");
		}
		
		return result;
	}

}
