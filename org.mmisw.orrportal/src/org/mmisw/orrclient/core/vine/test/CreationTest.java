package org.mmisw.orrclient.core.vine.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Skos;
import org.mmisw.ont.vocabulary.Vine;
import org.mmisw.orrclient.OrrClientFactory;
import org.mmisw.orrclient.core.util.ontinfo.OntInfoUtil;
import org.mmisw.orrclient.core.util.ontinfo.StmtKey;
import org.mmisw.orrclient.core.util.ontype.OntTypeUtil;
import org.mmisw.orrclient.core.vine.MappingOntologyCreator;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.CreateOntologyResult;
import org.mmisw.orrclient.gwt.client.rpc.MappingDataCreationInfo;
import org.mmisw.orrclient.gwt.client.rpc.MappingOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyType;
import org.mmisw.orrclient.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.mmisw.orrportal.gwt.server.OrrConfig;


/**
 * Tests for the VINE-based creation of mapping ontologies.
 *
 * Checks include: creation in the approproiate format;
 * given namespaces are owl:import'ed; mappings are correct.
 *
 * @author Carlos Rueda
 */
public class CreationTest extends VineTestCase {

	private final String authority = "mmitest";
	private final String shortName = "vinetest1";
	private final String ontologyUri = "http://localhost:8080/ont/" +authority+ "/" +shortName;

	private final String uri1 = "http://example.org/ontologyOne";
	private final String uri2 = "http://example.org/ontologyTwo";
	private final Set<String> uris = new HashSet<>(Arrays.asList(uri1, uri2));
	private final String namespace1 = uri1 + "/";
	private final String namespace2 = uri2 + "/";
	private final String[][]  mapps = {
		{ namespace1 + "termAAAAAA", Skos.exactMatch.getURI(), namespace2 + "termPPPPPP" },
		{ namespace1 + "termBBBBBB", Skos.closeMatch.getURI(), namespace2 + "termQQQQQQ" },
		{ namespace1 + "termCCCCCC", Skos.relatedMatch.getURI(), namespace2 + "termRRRRRR" },
	};

	private final String[]  comments = {
		"comment for AAAAAA-PPPPPP",
		"comment for BBBBBB-QQQQQQ",
		"comment for CCCCCC-RRRRRR",
	};

	private final String[]  confidences = {
			"100",
			"90",
			"80",
	};


	private final Set<StmtKey> originalTriples = new HashSet<StmtKey>();


	public void testCreate() throws Exception {
		OrrConfig.init();
		OrrClientFactory.init();

		CreateOntologyResult result = _createOntology(ontologyUri);
		assertNull("No error in creating mapping ontology", result.getError());

		File file = new File(result.getFullPath());

		if ( log.isDebugEnabled() ) {
			String rdf = IOUtils.toString(new FileInputStream(file), "utf-8");
			log.debug("RDF: \n" +rdf);
		}

		OntModel ontModel = _load(file);

		OntologyType ontype = OntTypeUtil.determineType(ontModel, ontologyUri, null);
		assertEquals("ontology type should be MAPPING", OntologyType.MAPPING, ontype);

		_verifyMappings(ontModel);
		_verifyImports(ontModel);
	}



	private CreateOntologyResult _createOntology(String ontologyUri) throws Exception {

		MappingDataCreationInfo mdci = new MappingDataCreationInfo();
		mdci.setUris(uris);
		List<Mapping> mappings = new ArrayList<Mapping>();
		mdci.setMappings(mappings);

		originalTriples.clear();
		for ( int i = 0; i < mapps.length; i++ ) {
			String[] mapp = mapps[i];
			Mapping mapping = new Mapping(mapp[0], mapp[1], mapp[2]);
			Map<String,String> mappingMetadata = new HashMap<String,String>();
			mappingMetadata.put(RDFS.comment.getURI(), comments[i]);
			mappingMetadata.put(Vine.confidence.getURI(), confidences[i]);
			mapping.setMetadata(mappingMetadata);
			mappings.add(mapping);
			originalTriples.add(new StmtKey(mapp[0], mapp[1], mapp[2]));
		}
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

	private void _verifyMappings(OntModel ontModel) throws Exception {
		// now, test correct extraction

		TempOntologyInfo baseOntologyInfo = new TempOntologyInfo();
		baseOntologyInfo.setUri(ontologyUri);
		OntInfoUtil.getEntities(baseOntologyInfo, ontModel);

		OntologyData od = baseOntologyInfo.getOntologyData();
		assertSame("ontology data should be mapping", MappingOntologyData.class, od.getClass());

		// check that all mappings in the model exactly correspond to the original ones:
		MappingOntologyData mod = (MappingOntologyData) od;
		List<Mapping> mappings = mod.getMappings();
		Set<StmtKey> retrievedTriples = new HashSet<StmtKey>();
		for ( int i = 0; i < mapps.length; i++ ) {
			Mapping mapping = mappings.get(i);
			retrievedTriples.add(new StmtKey(mapping.getLeft(), mapping.getRelation(), mapping.getRight()));
		}

		assertEquals(originalTriples.size(), retrievedTriples.size());
		for ( StmtKey originalKey : originalTriples ) {
			assertTrue("retrieved mapping is a original one", retrievedTriples.contains(originalKey));
		}
	}

	private void _verifyImports(OntModel ontModel) {
		Set<String> imported = ontModel.listImportedOntologyURIs();
		for ( String uri : uris ) {
			assertTrue("namespace is owl:import'ed", imported.contains(uri));
		}
	}
}

