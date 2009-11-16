package org.mmisw.mmiorr.client.test;

import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.mmisw.mmiorr.client.RegisterOntology;
import org.mmisw.mmiorr.client.RetrieveOntology;
import org.mmisw.mmiorr.client.RegisterOntology.RegistrationResult;
import org.mmisw.mmiorr.client.RetrieveOntology.RetrieveResult;

/**
 * @author Carlos Rueda
 */
public class MmiOrrTest extends TestCase {

	/**
	 * <a href="http://ci.oceanobservatories.org/tasks/browse/CIDEVDM-42">CIDEVDM-42</a>
	 * @throws Exception
	 */
	public void test42() throws Exception {
		String username = _getRequiredSystemProperty("username");
		String password = _getRequiredSystemProperty("password");
		
		String ontologyUri = System.getProperty("ontologyUri", "http://example.org/test1");
		String fileName = System.getProperty("fileName", "resource/test1.owl");
		String graphId = System.getProperty("graphId", "ooi-ci");
		
		String fileContents = IOUtils.toString(new FileReader(fileName));
		
		RegistrationResult regisResult = RegisterOntology.register(username, password, ontologyUri, fileName, fileContents, graphId);
		assertEquals(HttpStatus.SC_OK, regisResult.status);
		
		String format = "owl";
		String version = null;
		
		RetrieveResult retrResult = RetrieveOntology.retrieve(ontologyUri, version, format);
		assertEquals(HttpStatus.SC_OK, retrResult.status);
		
		// TODO: make logical comparison (using the Jena library).
		// ...
	}

	private String _getRequiredSystemProperty(String key) {
		String val = System.getProperty(key);
		if ( val == null || val.trim().length() == 0 ) {
			throw new IllegalArgumentException(key+ " not specified");
		}
		return val;
	}

}
