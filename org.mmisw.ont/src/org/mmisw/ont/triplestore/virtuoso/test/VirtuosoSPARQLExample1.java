package org.mmisw.ont.triplestore.virtuoso.test;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

import virtuoso.jena.driver.*;

/** Adapted from the Virtuoso-Jena examples */
class VirtuosoSPARQLExample1 {
	private static String _host = "jdbc:virtuoso://mmi2.shore.mbari.org:1111";
	private static String _username = System.getProperty("virtuoso.username");
	private static String _password = System.getProperty("virtuoso.password");

	/**
	 * Executes a SPARQL query against a virtuoso url and prints results.
	 */
	public static void main(String[] args) {

		VirtGraph set = new VirtGraph (_host, _username, _password);
		System.out.println("set.size = " +set.size());

/*		Select all data in virtuoso	*/
		Query sparql = QueryFactory.create("SELECT * WHERE { GRAPH ?graph { ?s ?p ?o } } limit 100");
//		Query sparql = QueryFactory.create("SELECT * WHERE { <http://localhost:8080/ont/mmitest/test-TDB/t1> ?p ?o . }");
//		Query sparql = QueryFactory.create("SELECT * where { <http://localhost:8080/ont/mmi/stdname/tendency_of_atmosphere_mass_content_of_hexachlorobiphenyl_due_to_emission> ?p ?o . }");

		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, set);

		ResultSet results = vqe.execSelect();
		System.out.println("RESULT:");
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
		    RDFNode graph = result.get("graph");
		    RDFNode s = result.get("s");
		    RDFNode p = result.get("p");
		    RDFNode o = result.get("o");
		    System.out.println(graph + " { " + s + " " + p + " " + o + " . }");
		}
	}
}
