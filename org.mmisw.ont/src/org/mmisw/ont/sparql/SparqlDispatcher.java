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
import org.mmisw.ont.OntServlet;
import org.mmisw.ont.triplestore.ITripleStore;
import org.mmisw.ont.util.Unfinished;
import org.mmisw.ont.util.Util;



/**
 * Dispatcher of SPARQL queries.
 * 
 * <p>
 * Note: This class is effectively a singleton as it is only instantiated once by {@link OntServlet}
 * (ie., the singleton-ness is not forced here).
 * 
 * <p>
 * Thread-safety: This class is not strictly thread-safe, but it is "effectively thread-safe"
 * in conjunction with {@link OntServlet} and other callers. 
 * 
 * @author Carlos Rueda
 */
@Unfinished(priority=Unfinished.Priority.MEDIUM)
public class SparqlDispatcher {
	
	private final Log log = LogFactory.getLog(SparqlDispatcher.class);
	
	private final ITripleStore tripleStore;
	
	public SparqlDispatcher(ITripleStore tripleStore) {
		this.tripleStore = tripleStore;
	}

	
	/** 
	 * Executes the query indicated as argument of the "sparql" parameter in the request.
	 * Return SC_BAD_REQUEST to the client if value not given.
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		String query = Util.getParam(request, "sparql", "");
		if ( query.length() == 0 ) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "sparql parameter value not given");
			return;
		}

		String form = Util.getParam(request, "form", null);
		_executeWithCompletion(request, response, query, null, form, true);
	}
	
	/** 
	 * Executes the given query.
	 * 
	 * @param requestedEntity If non-null and the result of the query is empty, then 404 is returned to the client.
	 * 
	 * @return 
	 *          true iff dispatch completed here. 
	 *          false iff requestedEntity is null AND query result is empty.
	 */
	public boolean execute(HttpServletRequest request, HttpServletResponse response, 
			String query, String requestedEntity,
			String outFormat
	)
	throws ServletException, IOException {
		
		return _executeWithCompletion(request, response, query, requestedEntity, outFormat, false);
	}
	
	
	/** 
	 * Executes the given query.
	 * 
	 * 
	 * @param request
	 * @param response
	 * @param query
	 * @param requestedEntity If non-null and the result of the query is empty, then 404 is returned to the client.
	 * @param outFormat
	 * @param forceCompletion  
	 *                true to force completion here (so the return will always be true); 
	 *                false to return false iff: requestedEntity is null AND query result is empty.
	 *                     
	 * @return 
	 *          true iff dispatch completed here. 
	 *          false iff !forceCompletion AND requestedEntity is null AND query result is empty.
	 *          
	 * @throws ServletException
	 * @throws IOException
	 */
	private boolean _executeWithCompletion(HttpServletRequest request, HttpServletResponse response, 
			String query, String requestedEntity,
			String outFormat, boolean forceCompletion
	)
	throws ServletException, IOException {
		
		response.setHeader("Access-Control-Allow-Origin", "*");

		QueryResult queryResult;
		try {
			queryResult = _execute(query, outFormat);
		}
		catch (Exception e) {
			String error = "ERROR: " +e.getMessage();
			log.error(error, e);
			error += "\nMore details of the error are in the ORR logs";
			if ( e.getCause() != null ) {
				String cause = e.getCause().getMessage();
				if ( cause != null )
					error += "\n" + cause;
			}
			StringReader is = new StringReader(error);
			ServletOutputStream os = response.getOutputStream();
			response.setContentType("text/plain");
			IOUtils.copy(is, os);
			os.close();
			return true;
		}
		
		// set the content type now (although this might be changed below)
		// (this should fix 284: "empty reponse with incorrect content type")
		response.setContentType(queryResult.getContentType());
		String result = queryResult.getResult();
		

		if ( queryResult.isEmpty() ) {
			if ( requestedEntity != null  ) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, requestedEntity);
				return true;
			}
			
			if ( ! forceCompletion ) {
				// we have the condition: !forceCompletion AND requestedEntity is null AND query result is empty.
				return false;  // dispatch NO completed here.
			}
		}
		
		if ( "Application/rdf+xml".equalsIgnoreCase(queryResult.getContentType()) ) {
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
		
		else if ( "text/html".equalsIgnoreCase(queryResult.getContentType()) ) {
			String queryComm = "\n<!-- Query:\n\n" +Util.toHtmlComment(query)+ "\n-->\n\n";
			String pre, pos;
			
			if ( "html-frag".equals(outFormat) ) {
				pre = queryComm;
				pos = "";
			}
			else {
				pre = "<html><head><title>Query result</title>" +
								"<link rel=stylesheet href=\"" +
								request.getContextPath()+ "/main.css\" type=\"text/css\">" +
								"</head><body>\n" +
						queryComm
				;
				pos = "</body></html>";
			}
			result = pre + result + pos;
			response.setContentType(queryResult.getContentType());
		}
		
		else {
			response.setContentType(queryResult.getContentType());
		}
		
		ServletOutputStream os = response.getOutputStream();
		IOUtils.write(result, os, "UTF-8");
		os.close();
		
		return true;
	}

	private QueryResult _execute(String sparqlQuery, String form) throws Exception {
		
		if ( log.isDebugEnabled() ) {
			log.debug("_execute: query string = [" +sparqlQuery+ "]");
		}
		long start = System.currentTimeMillis();
		QueryResult queryResult = tripleStore.executeQuery(sparqlQuery, form);

		if ( log.isDebugEnabled() ) {
			if ( queryResult.isEmpty() ) {
				log.debug(
						"result = EMPTY\n" +
						"_execute: query processed in " +Util.elapsedTime(start)
				);
			}
			else {
				String result = queryResult.getResult();
				int len = result.length();
				if ( len > 555 ) {
					result = result.substring(0, 200) + "\n...\n" + result.substring(len - 200);
				}
				log.debug(
						"result = [\n" +result+ "\n]\n" +
						"_execute: query processed in " +Util.elapsedTime(start)
				);
			}
		}
		
		return queryResult;
	}

}
