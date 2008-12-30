package org.mmisw.ont.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

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
		
		String form = Util.getParam(request, "form", null);
		
		Sparql.QueryResult queryResult = _execute(query, form);
		
		String result = queryResult.getResult();
		
		if ( "Application/rdf+xml".equals(queryResult.getContentType()) ) {
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
		}
		
		else if ( "text/html".equals(queryResult.getContentType()) ) {
			String pre = "<html><head><title>Query result</title>" +
			             "<link rel=stylesheet href=\"" +
			                  request.getContextPath()+ "/main.css\" type=\"text/css\">" +
			             "</head><body>\n";
			pre += "\n<!-- Query:\n" +Util.toHtmlComment(query)+ "\n-->\n\n";
			result = pre + result + "</body></html>";
			response.setContentType(queryResult.getContentType());
		}
		
		else {
			response.setContentType(queryResult.getContentType());
		}
		StringReader is = new StringReader(result);
		ServletOutputStream os = response.getOutputStream();
		IOUtils.copy(is, os);
		os.close();
	}

	private Sparql.QueryResult _execute(String sparqlQuery, String form) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("execute: query string = [" +sparqlQuery+ "]");
		}
		
		Sparql.QueryResult queryResult = Sparql.executeQuery(ontGraph.getModel(), sparqlQuery, form);

		if ( log.isDebugEnabled() ) {
			log.debug("execute: result = [" +queryResult.getResult()+ "]");
		}
		
		return queryResult;
	}

}
