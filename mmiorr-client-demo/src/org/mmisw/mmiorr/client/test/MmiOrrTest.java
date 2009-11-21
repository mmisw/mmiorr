package org.mmisw.mmiorr.client.test;

import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.mmisw.mmiorr.client.RegisterOntology;
import org.mmisw.mmiorr.client.RetrieveOntology;
import org.mmisw.mmiorr.client.RegisterOntology.RegistrationResult;
import org.mmisw.mmiorr.client.RetrieveOntology.RetrieveResult;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public class MmiOrrTest extends TestCase {

	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-42">CIDEVDM-42</a>
	 * @throws Exception
	 */
	public void test42() throws Exception {
		
		// registration:
		String username = Utils.getRequiredSystemProperty("username");
		String password = Utils.getRequiredSystemProperty("password");
		
		String ontologyUri = System.getProperty("ontologyUri", "http://example.org/test1");
		String fileName = System.getProperty("fileName", "resource/test1.owl");
		String graphId = System.getProperty("graphId", "ooi-ci");
		
		String fileContents = IOUtils.toString(new FileReader(fileName));
		
		RegistrationResult regisResult = RegisterOntology.register(username, password, ontologyUri, fileName, fileContents, graphId);
		assertEquals(HttpStatus.SC_OK, regisResult.status);
		assertNotNull(regisResult.message);
		assertTrue(regisResult.message.contains("<success>"));
		
		// retrieval:
		String format = "owl";
		String version = null;
		
		RetrieveResult retrResult = RetrieveOntology.retrieve(ontologyUri, version, format);
		assertEquals(HttpStatus.SC_OK, retrResult.status);
		assertNotNull(retrResult.body);
		assertTrue(retrResult.body.contains("<rdf:RDF"));
		
		// Verify that both models contain exactly the same statements, except for 
		// the ontology metadata.
		
		// first, create corresponding models:
		// model for the original file:
		OntModel model1 = Utils.readModel(new File(fileName));
		// model for the retrieved file:
		OntModel model2 = Utils.readModel(retrResult.body);
		
		// check that the models are isomorphic but ignoring all statements about
		// the ontology resource:
		Resource ontologyResource = ResourceFactory.createResource(ontologyUri);
		model1.removeAll(ontologyResource, null, null);
		model2.removeAll(ontologyResource, null, null);
		// .. and now compare
		assertTrue(model1.isIsomorphicWith(model2));
	}


}
