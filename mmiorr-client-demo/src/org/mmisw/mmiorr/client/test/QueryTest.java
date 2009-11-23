package org.mmisw.mmiorr.client.test;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.mmisw.mmiorr.client.RetrieveOntology;
import org.mmisw.mmiorr.client.RetrieveOntology.RetrieveResult;

/**
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public class QueryTest extends BaseTestCase {

	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-47">CIDEVDM-47</a>
	 * Demonstrattion of some SPARQL queries
	 * @throws Exception
	 */
	@Test
	public void test47() throws Exception {
		System.out.println("** test47");
		
		String format = "csv";
		
		String[] queries = {
				
				"SELECT ?p ?o " +
				"WHERE { " +
				"  <http://motherlode.ucar.edu:8080/thredds/fileServer/station/metar/Surface_METAR_20091106_0000.nc> " +
				"  ?p ?o }" ,
				
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"SELECT ?s " +
				"WHERE {?s rdfs:isDefinedBy <http://xmlns.com/foaf/0.1/>. } ",
				
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"SELECT ?comment " +
				"WHERE { <http://www.w3.org/2003/01/geo/wgs84_pos#> rdfs:comment ?comment } ",
		};
		
		for ( String query : queries ) {
			RetrieveResult result = RetrieveOntology.queryGet(query, format);
			assertEquals(HttpStatus.SC_OK, result.status);
			assertNotNull(result.body);

			System.out.println(result.body);
		}
		
	}

}
