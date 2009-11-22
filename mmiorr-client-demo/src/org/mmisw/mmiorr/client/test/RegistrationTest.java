package org.mmisw.mmiorr.client.test;

import java.io.FileReader;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mmisw.mmiorr.client.RegisterOntology;
import org.mmisw.mmiorr.client.RegisterOntology.RegistrationResult;

/**
 * Tests requiring login.
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public class RegistrationTest extends BaseTestCase {

	@Before
	protected void setUp() {
		username = Utils.getRequiredSystemProperty("username");
		password = Utils.getRequiredSystemProperty("password");
		super.setUp();
	}
	
	@Test
	public void testRegistration() throws Exception {
		System.out.println("** testRegistration");
		String fileContents = IOUtils.toString(new FileReader(fileName));
		RegistrationResult regisResult = RegisterOntology.register(username, password, ontologyUri, fileName, fileContents, graphId);
		assertEquals(HttpStatus.SC_OK, regisResult.status);
		assertNotNull(regisResult.message);
		System.out.println("Registration response:" +
				("\n" + regisResult.message).replaceAll("\n", "\n   ")
		);
		assertTrue(regisResult.message.contains("<success>"));
	}
	

}
