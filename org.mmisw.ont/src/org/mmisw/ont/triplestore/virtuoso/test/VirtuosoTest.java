package org.mmisw.ont.triplestore.virtuoso.test;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Some testing of Virtuoso's reasoning capabality: enabling and execution.
 * 
 * <p>
 * In summary, inference is enabled from the query itself (via corresponding
 * options), and this is in a NON STANDARD way.
 * 
 * See <a href="http://docs.openlinksw.com/virtuoso/virtuosofaq.html#virtuosofaq7"
 * >this FAQ</a>.
 * 
 * <p>
 * For example: <pre>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#> 
SELECT ?o 
WHERE { &lt;http://example.org/vocres/AAA>   
        skos:exactMatch   
        ?o   OPTION(TRANSITIVE)
} 
 * </pre>
 * 
 * @author Carlos Rueda
 */
class VirtuosoTest {

	private static final String TEST_GRAPH = "urn:exactmatch.test";

	private static final String TEST_MODEL = "onts/mapres-skos2004.owl";

	private static String _host = "jdbc:virtuoso://mmi2.shore.mbari.org:1111";
	private static String _username = System.getProperty("virtuoso.username");
	private static String _password = System.getProperty("virtuoso.password");
	
	/** test program */
	public static void main(String[] args) throws Exception {
		_clearGraph();
		_loadTestModel();
		_queryExactMatch();
		_queryWithInference();
	}
	
	static void _clearGraph() {
		VirtGraph _graph = new VirtGraph (TEST_GRAPH, _host, _username, _password);
		_graph.clear();
		System.out.println("clear done.  graph size = " +_graph.size());
		_graph.close();
	}
	
	/**
	 * Load a model that basically asserts:
	 * <ul>
	 * <li> AAA skos:exactMatch BBB
	 * <li> BBB skos:exactMatch CCC
	 * <li> CCC skos:exactMatch DDD
	 * </ul>
	 */
	static void _loadTestModel() {
		VirtGraph _graph = new VirtGraph (TEST_GRAPH, _host, _username, _password);
		Model _model = ModelFactory.createModelForGraph(_graph);
		
		_model.read("file:" +TEST_MODEL);
		System.out.println("Model read.  graph size = " +_graph.size());
		
		_graph.close();
	}

	/** { ?s skos:exactMatch ?o } */
	private static void _queryExactMatch() {
		VirtGraph _graph = new VirtGraph (TEST_GRAPH, _host, _username, _password);
		Query sparql = QueryFactory.create(
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
				"SELECT * WHERE { ?s skos:exactMatch ?o } ");

		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, _graph);

		ResultSet results = vqe.execSelect();
		System.out.println("RESULT:");
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
		    RDFNode s = result.get("s");
		    RDFNode o = result.get("o");
		    System.out.println("  " + s + " " + "skos:exactMatch" + " " + o);
		}
		
		vqe.close();
		_graph.close();

	}

	/**
	 * <pre>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#> 
SELECT ?o 
WHERE { &lt;http://example.org/vocres/AAA>   
        skos:exactMatch   
        ?o   OPTION(TRANSITIVE)
} 
	 * </pre>
	 * which returns:
	 * <pre>
RESULT:
 http://example.org/vocres/BBB
 http://example.org/vocres/CCC
 http://example.org/vocres/DDD
	 * </pre>
	 */
	private static void _queryWithInference() {
		VirtGraph _graph = new VirtGraph (TEST_GRAPH, _host, _username, _password);
		String queryString = 
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
			"SELECT ?o WHERE " +
			"{ <http://example.org/vocres/AAA> " +
			"  skos:exactMatch " +
			"  ?o " +
			"  OPTION(TRANSITIVE)" +  // <<-- Jena triggers error here; but Virtuoso handles it
			"} " 
		;
		
		// with the OPTION(TRANSITIVE) above, Jena triggers syntax error here:
//		Query sparql = QueryFactory.create(queryString);
		
		// but passing the string directly to Virtuoso is fine:
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create(
//				sparql,
				queryString,
				_graph);
		
		// and the result includes the inferred triples:
//		RESULT:
//			 http://example.org/vocres/BBB
//			 http://example.org/vocres/CCC
//			 http://example.org/vocres/DDD
				 
		ResultSet results = vqe.execSelect();
		System.out.println("RESULT:");
		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			RDFNode o = result.get("o");
			System.out.println(" "+ o );
		}
		
		vqe.close();
		_graph.close();
	}
}
