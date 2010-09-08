package org.mmisw.orrclient.core.vine.test;

import java.io.File;

import org.mmisw.orrclient.core.util.ontype.OntTypeUtil;
import org.mmisw.orrclient.gwt.client.rpc.OntologyType;

import com.hp.hpl.jena.ontology.OntModel;


/**
 * Tests for checking detection of VINE-created ontologies.
 * 
 * @author Carlos Rueda
 */
public class IsMappingTest extends VineTestCase {
	
	/** base dir for the resources needed/generated */
	private static final String BASE_DIR = "resource/vine/onts/";
	
	/** various files but with the same ontology URI */
	private static final String ONTOLOGY_URI = "http://localhost:8080/ont/mmitest/VineTest";

	
	public void testIsMapping001() throws Exception {
		_isMapping("vine001.owl", ONTOLOGY_URI);
	}
	
	public void testIsMapping002() throws Exception {
		_isMapping("vine002.owl", ONTOLOGY_URI);
	}
	
	// not giving the ontology URI explicitly
	public void testIsMapping002b() throws Exception {
		_isMapping("vine002.owl", null);
	}
	
	public void testIsMapping003() throws Exception {
		_isMapping("vine003.owl", ONTOLOGY_URI);
	}
	
	public void testIsMapping004() throws Exception {
		_isMapping("vine004.owl", ONTOLOGY_URI);
	}
	
	private void _isMapping(String simplename, String ontologyUri) throws Exception {
		if ( log.isDebugEnabled() ) {
			log.debug("simplename=" +simplename+ " , ontologyUri=" +ontologyUri);
		}
		String filename = BASE_DIR+ simplename;
		OntModel ontModel = _load(new File(filename));
		OntologyType ontype = OntTypeUtil.determineType(ontModel, ontologyUri, null);
		assertEquals("ontology type should be MAPPING", OntologyType.MAPPING, ontype);
	}
	
}