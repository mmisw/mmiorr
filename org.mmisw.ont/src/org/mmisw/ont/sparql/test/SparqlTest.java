package org.mmisw.ont.sparql.test;

import java.io.InputStream;
import java.io.StringWriter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.PrintUtil;

public class SparqlTest {
	
	public static void main(String[] args) {
		new SparqlTest();
	}
	
	
	SparqlTest() {
//		loadBooksModel();
		loadDeviceModel();
		
		testConstruct2();
//		testSelect2();
	}
	
	private Model model;
	
	private void loadBooksModel() {
		String pkgDir = getClass().getPackage().getName().replace('.', '/');
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				pkgDir + "/books.n3");
		
		model = ModelFactory.createDefaultModel();
		model.read(is, "", "N3");
		
		_listStatements(model);
	}
	
	private void loadDeviceModel() {
		String pkgDir = getClass().getPackage().getName().replace('.', '/');
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				pkgDir + "/device.owl");
		
		model = ModelFactory.createDefaultModel();
		model.read(is, null, null);
		
//		_listStatements(model);
	}
	
	
	void testSelect1() {
		loadBooksModel();

		String Q = 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
			"SELECT ?creator " +
			"WHERE { ?s dc:creator ?creator. }";
			
//			"SELECT ?s ?p ?o " +
//			"where { ?s ?p ?o. }";

		System.out.println("RESULTS:");
		
		Query query = QueryFactory.create(Q);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results	
		ResultSetFormatter.out(System.out, results, query);

		// Important - free up resources used running the query
		qe.close();
	}
	
	void testConstruct1() {
		loadBooksModel();
		
		String Q = 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
			"CONSTRUCT { ?s dc:creator ?o } " +
			"where {?s dc:creator ?o. }";
		
		System.out.println("RESULTS:");

		Query query = QueryFactory.create(Q);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		try {
			Model model_ = qe.execConstruct();
			
	//		_listStatements(model_);
	
			StringWriter writer = new StringWriter();
			model_.getWriter().write(model_, writer, null);
	
			String result = writer.getBuffer().toString();
			System.out.println(result);
		}
		finally {
			qe.close();
		}

	}


	
	void testSelect2() {
		loadDeviceModel();

		String Q = 
//			"SELECT ?s ?p ?cls " +
//			"WHERE { ?s ?p ?cls. }";
		
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
			"SELECT ?cls " +
			"WHERE { ?cls rdf:type owl:Class. }";

		System.out.println("RESULTS:");
		
		Query query = QueryFactory.create(Q);

		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results, query);

		qe.close();
	}
	
	void testConstruct2() {
		loadDeviceModel();
		
		String Q = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
			"CONSTRUCT { ?cls rdf:type owl:Class. } \n" +
			"WHERE { ?cls rdf:type owl:Class. }";
		
		System.out.println("QUERY:\n" +Q);
		System.out.println("RESULTS:");

		Query query = QueryFactory.create(Q);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		try {
			Model model_ = qe.execConstruct();
			
			_listStatements(model_);
	
//			StringWriter writer = new StringWriter();
//			model_.getWriter().write(model_, writer, null);
//	
//			String result = writer.getBuffer().toString();
//			System.out.println(result);
		}
		finally {
			qe.close();
		}

	}

	
	private static void _listStatements(Model model) {
		StmtIterator iter = model.listStatements();
		while (iter.hasNext()) {
			com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
			Resource sjt = sta.getSubject();
			System.out.println("      " + 
					PrintUtil.print(sjt)
					+ "   " +
					PrintUtil.print(sta.getPredicate().getURI())
					+ "   " +
					PrintUtil.print(sta.getObject().toString())
			);
		}
	}

}
