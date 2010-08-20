package org.mmisw.iserver.core.vine.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.mmisw.iserver.core.util.Skos;
import org.mmisw.iserver.core.util.ontype.OntTypeUtil;
import org.mmisw.iserver.core.vine.MappingOntologyCreator;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.iserver.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.iserver.gwt.client.rpc.OntologyType;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;

import com.hp.hpl.jena.ontology.OntModel;


/**
 * Tests for the VINE-based creation of mapping ontologies.
 * 
 * @author Carlos Rueda
 */
public class CreationTest extends VineTestCase {
	
	final String authority = "mmitest";
	final String shortName = "vinetest1";
	final String ontologyUri = "http://localhost:8080/ont/" +authority+ "/" +shortName;
	
	public void testCreate() throws Exception {
		
		CreateOntologyResult result = _createOntology(ontologyUri);
		assertNull("No error in creating mapping ontology", result.getError());
		
		File file = new File(result.getFullPath());
		
		if ( log.isDebugEnabled() ) {
			String rdf = IOUtils.toString(new FileInputStream(file), "utf-8");
			log.debug("RDF: \n" +rdf);
		}
		
		OntModel ontModel = _load(file);
		
		OntologyType ontype = OntTypeUtil.determineType(ontModel, ontologyUri);
		assertEquals("ontology type should be MAPPING", OntologyType.MAPPING, ontype);
	}

	private CreateOntologyResult _createOntology(String ontologyUri) throws Exception {
		MappingDataCreationInfo mdci = new MappingDataCreationInfo();
		List<Mapping> mappings = new ArrayList<Mapping>();
		mdci.setMappings(mappings);
		mappings.add(new Mapping("foo:term1", Skos.exactMatch.getURI(), "bar:term2"));
		
		CreateOntologyInfo coi = new CreateOntologyInfo();
		coi.setUri(ontologyUri);
		coi.setAuthority(authority);
		coi.setShortName(shortName);
		Map<String, String> metadataValues = new HashMap<String, String>();
		coi.setMetadataValues(metadataValues);
		
		MappingOntologyCreator creator = new MappingOntologyCreator(coi, mdci); 
		
		CreateOntologyResult result = new CreateOntologyResult();
		creator.createOntology(result);
		
		return result;
	}
}