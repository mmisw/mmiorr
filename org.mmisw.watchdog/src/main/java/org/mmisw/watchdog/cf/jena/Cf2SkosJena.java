package org.mmisw.watchdog.cf.jena;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mmisw.watchdog.cf.Cf2SkosBase;
import org.mmisw.watchdog.util.jena.SKOS;

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
 * Cf2Skos implementation based on Jena.
 * 
 * @author bermudez
 * @author carueda
 */
public class Cf2SkosJena extends Cf2SkosBase {
	
	protected void _doConvert() throws Exception {
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

	
	private void _createNewOntology() {

		// create ontology
		model = SKOS.getAnSKOSModel();
		
		model.setNsPrefix("", namespace);

		// creates top concept scheme
//		conceptScheme = model.createResource(NS + "cf", SKOS.ConceptScheme);
//		conceptScheme.addProperty(DC.creator, "Luis Bermudez MMI");
//		conceptScheme.addProperty(DC.date, (new java.text.SimpleDateFormat(
//				"yyyy-MM-dd'T'hh:mm:ss")).format(new Date(System
//				.currentTimeMillis())));
//		conceptScheme.addProperty(DC.description, "CF Terms");

		
		standardNameClass = model.createResource(namespace + "Standard_Name");
		
		currentTopConcept = model
				.createResource(namespace + "parameter", standardNameClass);
//				.createResource(NS + "parameter", skosConcept);
//				.createResource(NS + "parameter", SKOS.Concept);

		Statement stmt;
		
		stmt = model.createStatement(standardNameClass, RDF.type, OWL.Class);
		model.add(stmt);
		
		stmt = model.createStatement(standardNameClass, RDFS.subClassOf, SKOS.Concept);
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
		
		int numConcepts = 0;
		
		List<?> list = standard_name_table.getChildren("entry");
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Element ele = (Element)iterator.next();
			String id = (ele.getAttribute("id").getValue()).trim();

			String canonicalUnits = ele.getChildTextNormalize("canonical_units");
			//				String grib = ele.getChildTextNormalize("grib");
			//				String amip = ele.getChildTextNormalize("amip");

			String description = ele.getChildTextNormalize("description");

			String prefLabel = id.replace('_', ' ');

			Resource concept = model.createResource(namespace + id,
					standardNameClass);
			//						skosConcept);
			//						SKOS.Concept);

			concept.addProperty(SKOS.prefLabel, prefLabel);
			concept.addProperty(RDFS.label, id);
			concept.addProperty(canonical_units, canonicalUnits);
			concept.addProperty(RDFS.comment, description);

			currentTopConcept.addProperty(SKOS.narrower, concept);

			numConcepts++;
		}

		props.put("concepts", String.valueOf(numConcepts));
		

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
