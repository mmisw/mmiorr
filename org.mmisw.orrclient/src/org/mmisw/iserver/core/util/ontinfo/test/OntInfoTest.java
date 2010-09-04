package org.mmisw.iserver.core.util.ontinfo.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.mmisw.iserver.core.util.ontinfo.OntInfoUtil;
import org.mmisw.iserver.core.util.ontype.OntTypeUtil;
import org.mmisw.iserver.gwt.client.rpc.BaseOntologyData;
import org.mmisw.iserver.gwt.client.rpc.MappingOntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyData;
import org.mmisw.iserver.gwt.client.rpc.OntologyType;
import org.mmisw.iserver.gwt.client.rpc.OtherOntologyData;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.rpc.vine.Mapping;

import com.hp.hpl.jena.ontology.OntModel;


/**
 * Tests for for extraction of ontology info.
 * 
 * @author Carlos Rueda
 */
public class OntInfoTest extends OntInfoTestCase {
	
	/** base dirs for the resources needed/generated */
	private static final String BASE_DIR_OTHER = "resource/onts/";
	private static final String BASE_DIR_VINE = "resource/vine/onts/";
	
	/** various files but with the same ontology URI */
	private static final String ONTOLOGY_URI = "http://localhost:8080/ont/mmitest/VineTest";

	
	public void testOther001() throws Exception {
		String filename = BASE_DIR_OTHER+ "sweet_top.owl";
		OntModel ontModel = _load(new File(filename));
		
		String ontologyUri = "http://sweet.jpl.nasa.gov/2.0/top.owl";
		OntologyType ontype = OntTypeUtil.determineType(ontModel, ontologyUri);
		assertEquals("ontology type should be Other", OntologyType.OTHER, ontype);

		TempOntologyInfo baseOntologyInfo = new TempOntologyInfo();
		baseOntologyInfo.setUri(ontologyUri);
		OntInfoUtil.getEntities(baseOntologyInfo, ontModel);
		
		OntologyData od = baseOntologyInfo.getOntologyData();
		assertSame("ontology data should be Other", OtherOntologyData.class, od.getClass());
		
		OtherOntologyData mod = (OtherOntologyData) od;
		
		BaseOntologyData bod = mod.getBaseOntologyData();
		
		if ( log.isDebugEnabled() ) {
			log.debug("getClasses    =" +bod.getClasses());
			log.debug("getIndividuals=" +bod.getIndividuals());
			log.debug("getProperties =" +bod.getProperties());
		}

	}

	
	public void testMapping001() throws Exception {
		_mapping("vine001.owl", ONTOLOGY_URI, 
				"http://localhost:8080/ont/mmitest/parameter/t2",
				"http://www.w3.org/2004/02/skos/core#exactMatch",
				"http://localhost:8080/ont/mmitest/parameter/t2",
				"test comment", 
				"100", "http://mmisw.org/ont/mmi/vine/confidence"
		);
	}
	
	public void testMapping004() throws Exception {
		_mapping("vine004.owl", ONTOLOGY_URI, 
				"http://localhost:8080/ont/mmitest/test/term1",
				"http://www.w3.org/2008/05/skos#exactMatch",
				"http://localhost:8080/ont/mmitest/test/term3",
				"COMMENT", 
				"50", "http://marinemetadata.org/mmiws/20071128/vine#confidence");
	}
	
	private void _mapping(String simplename, String ontologyUri,
			String left, String rel, String right,
			String comment, String confidence, String confidenceProp
	) throws Exception {
		
		if ( log.isDebugEnabled() ) {
			log.debug("simplename=" +simplename+ " , ontologyUri=" +ontologyUri);
		}
		String filename = BASE_DIR_VINE+ simplename;
		OntModel ontModel = _load(new File(filename));
		
		OntologyType ontype = OntTypeUtil.determineType(ontModel, ontologyUri);
		assertEquals("ontology type should be MAPPING", OntologyType.MAPPING, ontype);

		TempOntologyInfo baseOntologyInfo = new TempOntologyInfo();
		baseOntologyInfo.setUri(ontologyUri);
		OntInfoUtil.getEntities(baseOntologyInfo, ontModel);
		
		OntologyData od = baseOntologyInfo.getOntologyData();
		assertSame("ontology data should be mapping", MappingOntologyData.class, od.getClass());
		
		MappingOntologyData mod = (MappingOntologyData) od;
		
		List<Mapping> mappings = mod.getMappings();
		if ( log.isDebugEnabled() ) {
			log.debug("mappings = " +mappings);
		}
		assertEquals(1, mappings.size());
		
		Mapping mapping = mappings.get(0);
		
		assertEquals(left, mapping.getLeft());
		assertEquals(rel, mapping.getRelation());
		assertEquals(right, mapping.getRight());
		
		Map<String, String> md = mapping.getMetadata();
		assertNotNull(md);
		
		assertEquals(comment, md.get("http://www.w3.org/2000/01/rdf-schema#comment"));
		assertEquals(confidence, md.get(confidenceProp));
	}
	
}