package org.mmisw.watchdog.onts.cf.jena;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mmisw.iserver.core.util.Skos;
import org.mmisw.watchdog.conversion.BaseConverter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;


/**
 * CF conversion implementation based on Jena.
 * 
 * @author bermudez
 * @author carueda
 */
public class CfConverterJena extends BaseConverter {
	
	protected void _doConvert() throws Exception {
		numConcepts = 0;
		_createNewOntology();
		_convert();
	}

	protected void _doSave() throws Exception {
		_saveNewOntology();
	}

	
	///////////////////////////////////////////////////////////////////////////////
	// private
	///////////////////////////////////////////////////////////////////////////////
	
	private Model model;

	private Resource standardNameClass;
	
	private Resource currentTopConcept;

	private Property canonical_units;
	
	/** created concepts in SKOS ontology */
	private int numConcepts;

	
	private void _createNewOntology() {

		// create ontology
		model = Skos.createModel();
		
		model.setNsPrefix("", namespace);

		// creates top concept scheme
//		conceptScheme = model.createResource(NS + "cf", SKOS.ConceptScheme);
//		conceptScheme.addProperty(DC.creator, "Luis Bermudez MMI");
//		conceptScheme.addProperty(DC.date, (new java.text.SimpleDateFormat(
//				"yyyy-MM-dd'T'hh:mm:ss")).format(new Date(System
//				.currentTimeMillis())));
//		conceptScheme.addProperty(DC.description, "CF Terms");

		
		standardNameClass = model.createResource(namespace + "Standard_Name");
		
		currentTopConcept = _createConcept(namespace + "parameter");

		Statement stmt;
		
		stmt = model.createStatement(standardNameClass, RDF.type, OWL.Class);
		model.add(stmt);
		
		stmt = model.createStatement(standardNameClass, RDFS.subClassOf, Skos.Concept);
		model.add(stmt);

		stmt = model.createStatement(standardNameClass, RDFS.label, "Standard Name");
		model.add(stmt);
		
		stmt = model.createStatement(standardNameClass, RDFS.label, "Standard Name");
		model.add(stmt);

		
		canonical_units = model.createProperty(namespace + "canonical_units");
		
		stmt = model.createStatement(canonical_units, RDF.type, OWL.DatatypeProperty);
		model.add(stmt);
		stmt = model.createStatement(canonical_units, RDFS.domain, standardNameClass);
		model.add(stmt);
		stmt = model.createStatement(canonical_units, RDFS.range, XSD.xstring);
		model.add(stmt);


	}

	private void _convert() throws Exception {
		SAXBuilder builder = new SAXBuilder();

		Document document = builder.build(new StringReader(inputContents));

		Element standard_name_table = document.getRootElement();

		_getProperty(standard_name_table, "version_number");
		_getProperty(standard_name_table, "last_modified");
		_getProperty(standard_name_table, "institution");
		_getProperty(standard_name_table, "contact");
		
		List<?> list = standard_name_table.getChildren("entry");
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Element ele = (Element)iterator.next();
			
			numEntries++;
			
			String id = (ele.getAttribute("id").getValue()).trim();

			String canonicalUnits = ele.getChildTextNormalize("canonical_units");
			//				String grib = ele.getChildTextNormalize("grib");
			//				String amip = ele.getChildTextNormalize("amip");

			String description = ele.getChildTextNormalize("description");

			String prefLabel = id.replace('_', ' ');

			Resource concept = _createConcept(namespace + id);

			concept.addProperty(Skos.prefLabel, prefLabel);
			concept.addProperty(RDFS.label, id);
			concept.addProperty(canonical_units, canonicalUnits);
			concept.addProperty(RDFS.comment, description);

			currentTopConcept.addProperty(Skos.narrower, concept);
		}

		props.put("entries", String.valueOf(numEntries));
		props.put("concepts", String.valueOf(numConcepts));
	}

	
	private Resource _createConcept(String uri) {
		Resource concept = model.createResource(uri, standardNameClass);
		numConcepts++;
		return concept;
	}

	private void _saveNewOntology() {

		RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
		writer.setProperty("showXmlDeclaration", "true");
		writer.setProperty("relativeURIs", "same-document,relative");
		writer.setProperty("xmlbase", namespace);
		try {
			FileOutputStream fo = new FileOutputStream(outputFile);
			// model.setNsPrefix("",NS);
			// model.write(fo,"RDF/XML-ABBREV");

			writer.write(model, fo, null);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

		_log("New SKOS Ontology saved in: " + outputFile);
		_log("Size of the new Ontology: " + model.size());
	}

}
