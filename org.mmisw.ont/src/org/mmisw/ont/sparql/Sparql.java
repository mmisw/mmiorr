package org.mmisw.ont.sparql;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.util.Unfinished;
import org.mmisw.ont.util.Util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Dispatcher of SPARQL queries.
 * 
 * @author Carlos Rueda
 */
@Unfinished(priority=Unfinished.Priority.MEDIUM)
public class Sparql {

	private static final Log log = LogFactory.getLog(Sparql.class);
	
	
	/**
	 * Executes a SPARQL query against the given model.
	 * 
	 * @param model
	 * @param sparqlQuery
	 * @param form Only used for a "select" query.
	 * @return
	 * @throws Exception 
	 */
	public static QueryResult executeQuery(Model model, String sparqlQuery, String form) throws Exception {
		QueryExecution qe = null;
		try {
			Query query = QueryFactory.create(sparqlQuery);
			qe = QueryExecutionFactory.create(query, model);
			if ( log.isDebugEnabled() ) {
				log.debug("QueryExecution class: " +qe.getClass().getName());
			}
			return executeQuery(query, qe, sparqlQuery, form);
		}
		catch ( Throwable thr ) {
			String error = "Error preparing query.";
			throw new Exception(error, thr);
		}
		finally {
			if ( qe != null ) {
				qe.close();
			}
		}
	}
	
	
	/**
	 * Executes a SPARQL query given a QueryExecution.
	 * 
	 * @param query
	 * @param qe
	 * @param sparqlQuery The string used to create the query
	 * @param form Only used for a "select" query.
	 * @return
	 * @throws Exception 
	 */
	public static QueryResult executeQuery(Query query, QueryExecution qe, String sparqlQuery, String form) throws Exception {
		QueryResult queryResult = new QueryResult();
		
		try {
			// CONSTRUCT or DESCRIBE
			if ( query.isConstructType() || query.isDescribeType() ) {
				Model model_;
				
				if ( query.isConstructType() ) {
					if ( log.isDebugEnabled() ) {
						log.debug("Executing construct: [" +sparqlQuery+ "] form=" +form);
					}
					model_ = qe.execConstruct();
					if ( log.isDebugEnabled() ) {
						log.debug("execConstruct returned.");
					}
				}
				else {
					// DESCRIBE
					if ( log.isDebugEnabled() ) {
						log.debug("Executing describe: [" +sparqlQuery+ "] form=" +form);
					}
					model_ = qe.execDescribe();
					if ( log.isDebugEnabled() ) {
						log.debug("execDescribe returned.");
					}
				}
				
				queryResult.setIsEmpty(model_.isEmpty());
				
				JenaUtil2.removeUnusedNsPrefixes(model_);
				
				String str;	
				
				if ( form == null || form.equalsIgnoreCase("owl") || form.equalsIgnoreCase("rdf") ) {
					str = JenaUtil2.getOntModelAsString(model_, "RDF/XML-ABBREV");
					queryResult.setContentType("Application/rdf+xml");
				}
				else if ( form.equalsIgnoreCase("n3") ) {
					str = JenaUtil2.getOntModelAsString(model_, "N3");
					queryResult.setContentType("text/plain");
				}
				else if ( form.equalsIgnoreCase("nt") ) {
					str = JenaUtil2.getOntModelAsString(model_, "N-TRIPLE");
					queryResult.setContentType("text/plain");
				}
				else if ( form.equalsIgnoreCase("ttl") ) {
					str = JenaUtil2.getOntModelAsString(model_, "TURTLE");
					queryResult.setContentType("text/plain");
				}
				else {
					str = JenaUtil2.getOntModelAsString(model_, "RDF/XML-ABBREV");
					queryResult.setContentType("Application/rdf+xml");
				}
				queryResult.setResult(str);
			}
			
			// SELECT
			else if ( query.isSelectType() ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Executing select: [" +sparqlQuery+ "] form=" +form);
				}
				ResultSet results = qe.execSelect();
				if ( log.isDebugEnabled() ) {
					log.debug("execSelect returned.");
				}
				boolean hasNext = results.hasNext();
				if ( log.isDebugEnabled() ) {
					log.debug("hasNext = " +hasNext);
				}
				queryResult.setIsEmpty(! hasNext);

				if ( form == null || form.startsWith("html") ) {
					queryResult.setContentType("text/html");
					queryResult.setResult(_htmlSelectResults(results));
				}
				else if ( form.equalsIgnoreCase("csv") ) {
					queryResult.setContentType("text/plain");
					queryResult.setResult(_csvSelectResults(results));
				}
				else if ( form.equalsIgnoreCase("json") ) {
					queryResult.setContentType("application/json");
					queryResult.setResult(_jsonSelectResults(results));
				}
				else {
					queryResult.setContentType("text/plain");
					if ( hasNext ) {
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ResultSetFormatter.out(os, results, query);
						queryResult.setResult(os.toString());
					}
					else {
						queryResult.setResult("");
					}
				}
			}
			
			
			// TODO handle other types of queries.
			else {
				String error = "Sorry, query type " +query.getQueryType()+ " not handled yet";
				if ( log.isWarnEnabled() ) {
					log.warn("error: " +error);
				}
				queryResult.setResult(error);
				queryResult.setContentType("text/plain");
			}
		}
		finally {
			if ( log.isDebugEnabled() ) {
				log.debug("Closing QueryExecution");
			}
			qe.close();
		}
		
		return queryResult;
	}

	/** Formats the results in HTML */
	private static String _htmlSelectResults(ResultSet results) {
		if ( log.isDebugEnabled() ) {
			log.debug("_htmlSelectResults");
		}		
		
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
		out.printf("<table class=\"inline\">%n");
		out.printf("<tr>%n");
		List<?> vars = results.getResultVars();
		for ( Object var: vars ) {
			out.printf("\t<th>%s</th>%n", Util.toHtml(var.toString()));
		}
		out.printf("</tr>%n");
		
		if ( results.hasNext() ) {
			while ( results.hasNext() ) {
				out.printf("<tr>%n");
				QuerySolution sol = results.nextSolution();
				Iterator<?> varNames = sol.varNames();
				while ( varNames.hasNext() ) {
					String varName = String.valueOf(varNames.next());
					String varValue = String.valueOf(sol.get(varName));
					
					String link = Util.getLink(varValue);
					if ( link != null ) {
						out.printf("\t<td><a href=\"%s\">%s</a></td>%n", link, Util.toHtml(varValue));
					}
					else {
						out.printf("\t<td>%s</td>%n", Util.toHtml(varValue));
					}
				}
				out.printf("</tr>%n");
			}
		}
		out.printf("</table>%n");
		
		return sw.toString();
	}
	
	/** Formats the results in JSON */
	private static String _jsonSelectResults(ResultSet results) {
		if ( log.isDebugEnabled() ) {
			log.debug("_jsonSelectResults");
		}		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(bos, results);
		return bos.toString();
	}	
	
	/** Formats the results in CSV */
	private static String _csvSelectResults(ResultSet results) {
		if ( log.isDebugEnabled() ) {
			log.debug("_csvSelectResults");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsCSV(bos, results);
		return bos.toString();
	}

	private Sparql() {}
}
