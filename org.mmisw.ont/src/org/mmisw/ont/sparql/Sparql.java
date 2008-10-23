package org.mmisw.ont.sparql;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.mmisw.ont.util.Unfinished;

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
	
	public static class QueryResult {
		private String result;
		private String contentType;

		void setResult(String result) {
			this.result = result;
		}

		void setContentType(String contentType) {
			this.contentType = contentType;
		}
		public String getResult() {
			return result;
		}

		public String getContentType() {
			return contentType;
		}
	}

	public static QueryResult executeQuery(Model model, String sparqlQuery) {
		QueryResult queryResult = new QueryResult();
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		
		try {
			if ( query.isConstructType() ) {
				Model model_ = qe.execConstruct();
				StringWriter writer = new StringWriter();
				model_.getWriter().write(model_, writer, null);
				queryResult.setResult(writer.getBuffer().toString());
				queryResult.setContentType("Application/rdf+xml");
			}
			else if ( query.isSelectType() ) {
				ResultSet results = qe.execSelect();
				
				if ( true ) {
					queryResult.setContentType("text/html");
					queryResult.setResult(_htmlSelectResults(results));
				}
				else {
					queryResult.setContentType("text/plain");
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ResultSetFormatter.out(os, results, query);
					queryResult.setResult(os.toString());
				}
			}
			
			// TODO handle other types of queries.
			else {
				queryResult.setResult("Sorry, query type " +query.getQueryType()+ " not handled yet");
				queryResult.setContentType("text/plain");
			}
		}
		finally {
			qe.close();
		}
		
		return queryResult;
	}

	/** Formats the results in HTML */
	private static String _htmlSelectResults(ResultSet results) {
		
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
		out.printf("<table class=\"inline\">%n");
		out.printf("<tr>%n");
		List<?> vars = results.getResultVars();
		for ( Object var: vars ) {
			out.printf("<th>%s</th>%n", var.toString());
		}
		out.printf("</tr>%n");
		
		if ( results.hasNext() ) {
			while ( results.hasNext() ) {
				out.printf("<tr>%n");
				QuerySolution sol = results.nextSolution();
				Iterator<?> varNames = sol.varNames();
				while ( varNames.hasNext() ) {
					String varName = varNames.next().toString();
					out.printf("<td>%s</td>", sol.get(varName).toString());
				}
				out.printf("</tr>%n");
			}
		}
		out.printf("</table>%n");
		
		return sw.toString();
	}

	private Sparql() {}
}
