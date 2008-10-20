package org.mmisw.ont.sparql;

import java.io.StringWriter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class Sparql {

	public static String getRDF(Model model, String sparqlQuery) {
		
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExec = QueryExecutionFactory.create(query, model);
		
		try {
			Model model_ = qExec.execConstruct();
			StringWriter writer = new StringWriter();
			model_.getWriter().write(model_, writer, null);

			String result = writer.getBuffer().toString();
			return result;
		}
		finally {
			qExec.close();
		}
	}

	private Sparql() {}
}
