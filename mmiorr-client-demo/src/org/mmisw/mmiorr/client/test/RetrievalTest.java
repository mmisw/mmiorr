package org.mmisw.mmiorr.client.test;

import java.io.File;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.mmisw.mmiorr.client.RetrieveOntology;
import org.mmisw.mmiorr.client.RetrieveOntology.RetrieveResult;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public class RetrievalTest extends BaseTestCase {

	@Test
	public void testRetrievalOfOntology() throws Exception {
		System.out.println("** testRetrievalOfOntology");
		String format = "owl";
		String version = null;
		
		RetrieveResult retrievalResult = RetrieveOntology.retrieveOntology(ontologyUri, version, format);
		assertEquals(HttpStatus.SC_OK, retrievalResult.status);
		assertNotNull(retrievalResult.body);
		assertTrue(retrievalResult.body.contains("<rdf:RDF"));
		retrievedOntologyContents = retrievalResult.body;
	}

	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-42">CIDEVDM-42</a>
	 * @throws Exception
	 */
	@Test
	public void test42() throws Exception {
		System.out.println("** test42");
		
		// verification:
		// Verify that both models contain exactly the same statements, except for 
		// the ontology metadata.
		
		// first, create corresponding models:
		// model for the original file:
		OntModel model1 = Utils.readOntModel(new File(fileName));
		// model for the retrieved file:
		OntModel model2 = Utils.readOntModel(retrievedOntologyContents);
		
		// check that the models are isomorphic but ignoring all statements about
		// the ontology resource:
		Resource ontologyResource = ResourceFactory.createResource(ontologyUri);
		model1.removeAll(ontologyResource, null, null);
		model2.removeAll(ontologyResource, null, null);
		// .. and now compare
		assertTrue(model1.isIsomorphicWith(model2));
	}


	@Test
	public void testRetrievalOfTerm() throws Exception {
		System.out.println("** testRetrievalOfTerm");
		String format = "owl";
		String version = null;
		
		RetrieveResult retrievalResult = RetrieveOntology.retrieveOntology(termUri, version, format);
		assertEquals(HttpStatus.SC_OK, retrievalResult.status);
		assertNotNull(retrievalResult.body);
		System.out.println("Term retrieval response:" +
				("\n" + retrievalResult.body).replaceAll("\n", "\n   ")
		);
		assertTrue(retrievalResult.body.contains("<rdf:RDF"));
		retrievedTermContents = retrievalResult.body;
	}


	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-30">CIDEVDM-30</a>
	 * @throws Exception
	 */
	@Test
	public void test30() throws Exception {
		System.out.println("** test30");
		
		Model termModel = Utils.readTermModel(retrievedTermContents);
		Resource termResource = ResourceFactory.createResource(termUri);
		Property name = ResourceFactory.createProperty(nameUri);
		Property description = ResourceFactory.createProperty(descriptionUri);

		assertTrue( termModel.contains(termResource, RDFS.label, "termThree") );
		assertTrue( termModel.contains(termResource, name, "termThree") );
		assertTrue( termModel.contains(termResource, description, "description of termThree") );
		
	}
	


	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-30">CIDEVDM-32</a>
	 * This test is done with a fully-hosted vocabulary.
	 * 32a: retrieval of all versions associated with an ontology URI
	 * @throws Exception
	 */
	@Test
	public void test32a() throws Exception {
		System.out.println("** test32a");
		
		List<String> versions = RetrieveOntology.getVersions(vocabularyUri);
		System.out.println(vocabularyUri+ ": Available versions: " +versions);
		
		// retrieve each of the available versions:
		// note, no need to pass the "version" parameter because the URI is already in versioned form.
		for ( String vocabularyVersion : versions ) {
			RetrieveResult retrievalResult = RetrieveOntology.retrieveOntology(vocabularyVersion, null, "owl");
			assertEquals(HttpStatus.SC_OK, retrievalResult.status);
			assertNotNull(retrievalResult.body);
			assertTrue(retrievalResult.body.contains("<rdf:RDF"));
		}
	}

	
	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-30">CIDEVDM-32</a>
	 * Retrieves metadata directly from a fully-hosted ontology.
	 * @throws Exception
	 */
	private void retrieveMetadataFromOntology() throws Exception {
		System.out.println("** retrieveMetadataFromOntology");
		
		String format = "owl";
		String version = null;
		
		RetrieveResult retrievalResult = RetrieveOntology.retrieveOntology(vocabularyUri, version, format);
		assertEquals(HttpStatus.SC_OK, retrievalResult.status);
		assertNotNull(retrievalResult.body);
		assertTrue(retrievalResult.body.contains("<rdf:RDF"));
		OntModel model = Utils.readOntModel(retrievalResult.body);
		
		// the following shows the metadata ie. properties with vocabularyUri as subject:
		// false: not included to avoid cluttering the test output
		if ( false ) {
			Resource ontologyResource = ResourceFactory.createResource(vocabularyUri);
			StmtIterator metadata = model.listStatements(ontologyResource, null, (RDFNode) null);
			while ( metadata.hasNext() ) {
				Statement stmt = metadata.nextStatement();
				System.out.println("\t" +stmt.getPredicate()+ " -> " +stmt.getObject());
			}
		}
	}
	
	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-30">CIDEVDM-32</a>
	 * Retrieves metadata via a DESCRIBE query.
	 * @throws Exception
	 */
	private void retrieveMetadataViaDescribe() throws Exception {
		System.out.println("** retrieveMetadataViaDescribe");
		
		RetrieveResult describeResult = RetrieveOntology.describeUri(vocabularyUri, "owl");
		assertEquals(HttpStatus.SC_OK, describeResult.status);
		assertNotNull(describeResult.body);
		assertTrue(describeResult.body.contains("<rdf:RDF"));
		Model describeModel = Utils.readModel(describeResult.body);

		// the following shows the metadata ie. properties with vocabularyUri as subject:
		// false: not included to avoid cluttering the test output
		if ( false ) {
			Resource ontologyResource = ResourceFactory.createResource(vocabularyUri);
			StmtIterator describeMetatada = describeModel.listStatements(ontologyResource, null, (RDFNode) null);
			while ( describeMetatada.hasNext() ) {
				Statement stmt = describeMetatada.nextStatement();
				System.out.println("\t" +stmt.getPredicate()+ " -> " +stmt.getObject());
			}
		}
	}
	
	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-30">CIDEVDM-32</a>
	 * This test is done with a fully-hosted vocabulary.
	 * 32b: retrieval of metadata associated with an ontology URI: two ways: from the
	 * ontology itself, and via a DESCRIBE query.
	 * @throws Exception
	 */
	@Test
	public void test32b() throws Exception {
		System.out.println("** test32b");
		
		retrieveMetadataFromOntology();
		
		retrieveMetadataViaDescribe();
	}

}
