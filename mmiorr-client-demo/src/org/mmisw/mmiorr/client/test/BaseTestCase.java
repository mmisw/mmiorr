package org.mmisw.mmiorr.client.test;

import org.junit.Before;

import junit.framework.TestCase;

/**
 * Common stuff for the tests.
 * See build.xml for the way some parameters are passed
 * 
 * @author Carlos Rueda
 */
public abstract class BaseTestCase extends TestCase {

	protected static String username;
	protected static String password;
	
	protected static String ontologyUri;
	protected static String fileName;
	protected static String graphId;
	
	protected static String termUri;
	protected static String nameUri;
	protected static String descriptionUri;
	
	
	protected static String vocabularyUri;
	
	
	// obtained by testRetrievalOfOntology()
	protected static String retrievedOntologyContents;
	
	// obtained by testRetrievalOfTerm()
	protected static String retrievedTermContents;
	

	@Before
	protected void setUp() {
		ontologyUri = System.getProperty("ontologyUri", "http://example.org/test1");
		termUri = System.getProperty("termUri", "http://example.org/test1/termThree");
		nameUri = System.getProperty("nameUri", "http://example.org/test1/name");
		descriptionUri = System.getProperty("descriptionUri", "http://example.org/test1/description");
		fileName = System.getProperty("fileName", "resource/test1.owl");
		graphId = System.getProperty("graphId", "ooi-ci");
		
		vocabularyUri = System.getProperty("vocabularyUri", "http://mmisw.org/ont/mmi/device");
	}
	

}
