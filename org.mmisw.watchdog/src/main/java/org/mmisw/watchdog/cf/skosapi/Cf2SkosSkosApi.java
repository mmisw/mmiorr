package org.mmisw.watchdog.cf.skosapi;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mmisw.watchdog.cf.Cf2SkosBase;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.skos.AddAssertion;
import org.semanticweb.skos.SKOSChange;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSConceptScheme;
import org.semanticweb.skos.SKOSDataFactory;
import org.semanticweb.skos.SKOSDataRelationAssertion;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSEntityAssertion;
import org.semanticweb.skos.SKOSObjectRelationAssertion;
import org.semanticweb.skosapibinding.SKOSFormatExt;
import org.semanticweb.skosapibinding.SKOSManager;


/**
 * Cf2Skos implementation based on SKOS API.
 * 
 * @author Carlos Rueda
 */
public class Cf2SkosSkosApi extends Cf2SkosBase {
	
	protected void _doConvert() throws Exception {
		_createNewOntology();
		_convert();
	}

	protected void _doSave() throws Exception {
		_saveOntology();
	}

	
	///////////////////////////////////////////////////////////////////////////////
	// private
	///////////////////////////////////////////////////////////////////////////////
	
	private static final String CONCEPT_SCHEME = "CfStdName";
	private static final String TOP_CONCEPT = "Standard_Name";


	private Element standard_name_table;
	
	
	private SKOSManager manager;
	private SKOSDataset dataset;
	private SKOSDataFactory dataFactory;
	private List<SKOSEntity> allEntities;
	private SKOSConceptScheme conceptScheme;
	private Set<SKOSConcept> concepts;
	private SKOSConcept topConcept;
	private List<SKOSObjectRelationAssertion> objectRelationAssertions;
	private List<SKOSDataRelationAssertion> dataRelationAssertions;

	
	// OWL API stuff
	private OWLOntologyManager owlManager;
	private OWLOntology owlOntology;
	private OWLDataFactory owlDataFactory;
	private OWLDataProperty canonicalUnitsProp;
	private OWLDataProperty gribProp;
	private OWLDataProperty amipProp;
	private OWLDataProperty rdfsLabel;
	private OWLDataProperty rdfsComment;
	
	private List<AddAxiom> owlChanges;


	private void _createNewOntology() throws Exception {
		
		assert namespace.matches(".*(/|#)") ;
		
		String baseUri = namespace.replaceAll("(/|#)+$", "");  //removing any trailing slash/hash
		
		manager = new SKOSManager();

		if ( namespace.endsWith("#") ) {
			// Ok, OWLAPI v2 works fine with hash separator:
			dataset = manager.createSKOSDataset(URI.create(baseUri));
		}
		else {
			/////////////////////////////////////////////////////////////////////////////////////////////
			// TODO NOTE: workaround for bug in OWLAPI v2:
			// Instead of using a baseUri without separator,
			// use the namespace itself (ie, w/ fragment separator):
			dataset = manager.createSKOSDataset(URI.create(namespace));
			// In this way, the resulting ontology looks OK except that the xml:base is
			// written with the separator:
			//		
			//		<rdf:RDF xmlns="http://mmisw.org/ont/mmi/cf/parameter/"
			//		     xml:base="http://mmisw.org/ont/mmi/cf/parameter/"    <<<--- WITH trailing slash
			//		     xmlns:owl2xml="http://www.w3.org/2006/12/owl2-xml#"
			//		     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
			//		     xmlns:owl2="http://www.w3.org/2006/12/owl2#"
			//		     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
			//		     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
			//		     xmlns:owl="http://www.w3.org/2002/07/owl#"
			//		     xmlns:skos="http://www.w3.org/2004/02/skos/core#">
			//		    <owl:Ontology rdf:about=""/>
			//		...
			//	    <!-- http://mmisw.org/ont/mmi/cf/parameter/CfStdName -->
			//
			//	        <skos:ConceptScheme rdf:about="CfStdName"/>
			//	        
			//
			//	        <!-- http://mmisw.org/ont/mmi/cf/parameter/age_of_stratospheric_air -->
			//
			//	        <skos:Concept rdf:about="age_of_stratospheric_air">
			//	            <skos:inScheme rdf:resource="CfStdName"/>
			//	        </skos:Concept>
			//	        
			//
			//	        <!-- http://mmisw.org/ont/mmi/cf/parameter/air_density -->
			//
			//	        <skos:Concept rdf:about="air_density">
			//	            <skos:inScheme rdf:resource="CfStdName"/>
			//	        </skos:Concept>
			//
			//		...
			/////////////////////////////////////////////////////////////////////////////////////////////
		}
		
		
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new StringReader(inputContents));

		standard_name_table = document.getRootElement();
		
		_getProperty(standard_name_table, "version_number");
		_getProperty(standard_name_table, "last_modified");

		// TODO assign version_number, last_modified as some properties to the ontology itself.
		
        dataFactory = manager.getSKOSDataFactory();

        allEntities = new ArrayList<SKOSEntity>();
        conceptScheme = dataFactory.getSKOSConceptScheme(URI.create(namespace + CONCEPT_SCHEME));
        concepts = new HashSet<SKOSConcept>();
        
        URI topConceptUri = URI.create(namespace + TOP_CONCEPT);
		topConcept = dataFactory.getSKOSConcept(topConceptUri);
		concepts.add(topConcept);
		
		objectRelationAssertions = new ArrayList<SKOSObjectRelationAssertion>();
		dataRelationAssertions = new ArrayList<SKOSDataRelationAssertion>();

		// skos:hasTopConcept
		objectRelationAssertions.add( dataFactory.getSKOSObjectRelationAssertion(conceptScheme, dataFactory.getSKOSHasTopConceptProperty(), topConcept));
	
		
		/////////////////////////////////////////////////
		// OWL API stuff
		owlManager = manager.getOWLManger();
		owlOntology = owlManager.getOntology(URI.create(namespace));
		owlDataFactory = owlManager.getOWLDataFactory();

		canonicalUnitsProp = owlDataFactory.getOWLDataProperty(URI.create(namespace + "canonical_units"));
		gribProp = owlDataFactory.getOWLDataProperty(URI.create(namespace + "grib"));
		amipProp = owlDataFactory.getOWLDataProperty(URI.create(namespace + "amip"));
		
		rdfsLabel = owlDataFactory.getOWLDataProperty(URI.create("http://www.w3.org/2000/01/rdf-schema#label"));
		rdfsComment = owlDataFactory.getOWLDataProperty(URI.create("http://www.w3.org/2000/01/rdf-schema#comment"));
		
		owlChanges = new ArrayList<AddAxiom>();
		
		_addOwlChange(topConceptUri, TOP_CONCEPT.replace('_', ' '), "", null, null, null);
		
	}

	/**
	 * Gets the value of an entity and put the corresp. entry in the props map.
	 * @param standard_name_table
	 * @param propName
	 */
	private void _getProperty(Element standard_name_table, String propName) {
		Iterator<?> iterator = standard_name_table.getChildren(propName).iterator();
		if ( iterator.hasNext() ) {
			Element ele = (Element)iterator.next();
			String propValue = ele.getTextNormalize();
			props.put(propName, propValue);
		}
	}

	private void _convert() throws Exception {
		
		List<?> list = standard_name_table.getChildren("entry");
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Element ele = (Element)iterator.next();
			String id = ele.getAttribute("id").getValue().trim();

			String canonicalUnits = ele.getChildTextNormalize("canonical_units");
			String grib = ele.getChildTextNormalize("grib");
			String amip = ele.getChildTextNormalize("amip");

			String description = ele.getChildTextNormalize("description");

			String prefLabel = id.replace('_', ' ');

			URI conceptUri = URI.create(namespace + id);
			
			SKOSConcept concept = dataFactory.getSKOSConcept(conceptUri);
			concepts.add(concept);

			// skos:narrower
			objectRelationAssertions.add(dataFactory.getSKOSObjectRelationAssertion(topConcept, dataFactory.getSKOSNarrowerProperty(), concept));

			// skos:prefLabel
			dataRelationAssertions.add(
					dataFactory.getSKOSDataRelationAssertion(
							concept, dataFactory.getSKOSPrefLabelProperty(), prefLabel
			));


			////////////////////////////////////////////////
			// OWL API stuff
			_addOwlChange(conceptUri, id, description, canonicalUnits, grib, amip);
			

		}

		props.put("concepts", String.valueOf(concepts.size()));
		
		allEntities.add(conceptScheme);
		allEntities.addAll(concepts);

		
		List<SKOSChange> skosChanges = new ArrayList<SKOSChange>();

		// entities:
		List<SKOSEntityAssertion> entityAssertions = dataFactory.getSKOSEntityAssertions(allEntities);
		for (SKOSEntityAssertion ass : entityAssertions) {
			skosChanges.add(new AddAssertion(dataset, ass));
		}

		// skos:inScheme
		objectRelationAssertions.addAll( 
			dataFactory.getSKOSObjectRelationAssertions(concepts, dataFactory.getSKOSInSchemeProperty(), conceptScheme)
		);

		// object relations:
		for (SKOSObjectRelationAssertion assertion : objectRelationAssertions) {
			skosChanges.add(new AddAssertion(dataset, assertion));
		}

		// data relations:
		for (SKOSDataRelationAssertion assertion : dataRelationAssertions) {
			skosChanges.add(new AddAssertion(dataset, assertion));
		}
		
		manager.applyChanges(skosChanges);
		
		
		////////////////////////////////////////////////////
		// OWL API assertions
		owlManager.applyChanges(owlChanges);
	}

	
	private void _addOwlChange(URI conceptUri, String id, String description, 
			String canonicalUnits, String grib, String amip) {
		
		OWLIndividual conceptOwlIndiv = owlDataFactory.getOWLIndividual(conceptUri); 
		
		// rdfs:label
		if ( id.trim().length() > 0 ) {
			OWLTypedConstant owlc = owlDataFactory.getOWLTypedConstant(id.trim());
			OWLDataPropertyAssertionAxiom addProp = owlDataFactory.getOWLDataPropertyAssertionAxiom(
					conceptOwlIndiv, rdfsLabel, owlc);
			owlChanges.add(new AddAxiom(owlOntology, addProp));
		}

		// rdfs:comment
		if ( description.trim().length() > 0 ) {
			OWLTypedConstant owlc = owlDataFactory.getOWLTypedConstant(description.trim());
			OWLDataPropertyAssertionAxiom addProp = owlDataFactory.getOWLDataPropertyAssertionAxiom(
					conceptOwlIndiv, rdfsComment, owlc);
			owlChanges.add(new AddAxiom(owlOntology, addProp));
		}
		
		// canonicalUnits
		if ( canonicalUnits != null && canonicalUnits.trim().length() > 0 ) {
			OWLTypedConstant owlc = owlDataFactory.getOWLTypedConstant(canonicalUnits.trim());
			OWLDataPropertyAssertionAxiom addProp = owlDataFactory.getOWLDataPropertyAssertionAxiom(
					conceptOwlIndiv, canonicalUnitsProp, owlc);
			owlChanges.add(new AddAxiom(owlOntology, addProp));
		}
		
		// grib
		if ( grib != null && grib.trim().length() > 0 ) {
			OWLTypedConstant owlc = owlDataFactory.getOWLTypedConstant(grib.trim());
			OWLDataPropertyAssertionAxiom addProp = owlDataFactory.getOWLDataPropertyAssertionAxiom(
					conceptOwlIndiv, gribProp, owlc);
			owlChanges.add(new AddAxiom(owlOntology, addProp));
		}
		// amip
		if ( amip != null && amip.trim().length() > 0 ) {
			OWLTypedConstant owlc = owlDataFactory.getOWLTypedConstant(amip.trim());
			OWLDataPropertyAssertionAxiom addProp = owlDataFactory.getOWLDataPropertyAssertionAxiom(
					conceptOwlIndiv, amipProp, owlc);
			owlChanges.add(new AddAxiom(owlOntology, addProp));
		}
	}

	private void _saveOntology() throws Exception {

		if ( outputFile.toLowerCase().startsWith("file:") ) {
			outputFile = outputFile.substring("file:".length());
		}
		File file = new File(outputFile);
		manager.save(dataset, SKOSFormatExt.RDFXML, URI.create("file:" + file.getAbsolutePath()));
	}

}
