package org.mmisw.mmiorr.client.test;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mmisw.mmiorr.client.RegisterOntology;
import org.mmisw.mmiorr.client.RetrieveOntology;
import org.mmisw.mmiorr.client.RegisterOntology.RegistrationResult;
import org.mmisw.mmiorr.client.RetrieveOntology.RetrieveResult;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Tests involving retrieval, update, and registration of updated information.
 * 
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public class UpdateTest extends BaseTestCase {

	@Before
	protected void setUp() {
		username = Utils.getRequiredSystemProperty("username");
		password = Utils.getRequiredSystemProperty("password");
		super.setUp();
	}
	
	private OntModel retrieveOntology() throws Exception {
		System.out.println("** retrieveOntology");
		String format = "owl";
		String version = null;
		
		RetrieveResult retrievalResult = RetrieveOntology.retrieve(ontologyUri, version, format);
		assertEquals(HttpStatus.SC_OK, retrievalResult.status);
		assertNotNull(retrievalResult.body);
		assertTrue(retrievalResult.body.contains("<rdf:RDF"));
		
		OntModel model = Utils.readOntModel(retrievalResult.body);

		return model;
	}
	
	private void registerOntology(OntModel model) throws Exception {
		System.out.println("** registerOntology");
		String fileContents = Utils.getOntModelAsString(model, "RDF/XML-ABBREV");
		RegistrationResult regisResult = RegisterOntology.register(username, password, ontologyUri, fileName, fileContents, graphId);
		assertEquals(HttpStatus.SC_OK, regisResult.status);
		assertNotNull(regisResult.message);
		System.out.println("Registration response:" +
				("\n" + regisResult.message).replaceAll("\n", "\n   ")
		);
		assertTrue(regisResult.message.contains("<success>"));

	}

	/**
	 * Demontrates the addition of a property to a resource.
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-31">CIDEVDM-31</a>
	 * @throws Exception
	 */
	@Test
	public void test31() throws Exception {
		System.out.println("** test31");
		
		OntModel model = retrieveOntology();

		// add a new statement for termUri in the model:
		Resource termResource = ResourceFactory.createResource(termUri);
		Property description = ResourceFactory.createProperty(descriptionUri);
		String descriptionString = "Generated description " +System.currentTimeMillis();
		Literal descriptionObject = ResourceFactory.createPlainLiteral(descriptionString);
		Statement statement = ResourceFactory.createStatement(termResource, description, descriptionObject);
		model.add(statement);
		System.out.println("New statement: " +statement);
		
		// register a new version with this updated model:
		registerOntology(model);
		
		// retrieve model again a verify that it contains our new statement
		OntModel model2 = retrieveOntology();
		assertTrue( model2.contains(statement) );
	}

}
