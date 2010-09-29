package org.mmisw.watchdog.onts.udunits.jena;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mmisw.ont.vocabulary.Skos;
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
 * UDUnits prefix conversion.
 * 
 * @author Carlos Rueda
 */
public class UdunitsPrefixConverterJena extends BaseConverter {

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

	private Resource siPrefixClass;
	

	private Property nameProperty;
	private Property valueProperty;
	private Property symbolProperty;
	
	/** created concepts in SKOS ontology */
	private int numConcepts;

	
	private void _createNewOntology() {

		// create ontology
		model = Skos.createModel();
		
		model.setNsPrefix("", namespace);

		
		siPrefixClass = model.createResource(namespace + "Prefix");
		
		Statement stmt;
		
		stmt = model.createStatement(siPrefixClass, RDF.type, OWL.Class);
		model.add(stmt);
		
		stmt = model.createStatement(siPrefixClass, RDFS.subClassOf, Skos.Concept);
		model.add(stmt);

		stmt = model.createStatement(siPrefixClass, RDFS.label, "Prefix");
		model.add(stmt);

		
		Property[] props = { 
				nameProperty = model.createProperty(namespace + "name"),
				valueProperty = model.createProperty(namespace + "value"),
				symbolProperty = model.createProperty(namespace + "symbol"),
		};
		for ( Property prop : props ) {
			stmt = model.createStatement(prop, RDF.type, OWL.DatatypeProperty);
			model.add(stmt);
			stmt = model.createStatement(prop, RDFS.domain, siPrefixClass);
			model.add(stmt);
			stmt = model.createStatement(prop, RDFS.range, XSD.xstring);
			model.add(stmt);
		}

	}

	private void _convert() throws Exception {
		SAXBuilder builder = new SAXBuilder();

		Document document = builder.build(new StringReader(inputContents));

		Element root = document.getRootElement();

		// note: there are not properities for the rot element in the prefixes XML
		//_getProperty(root, "version_number");
		
		List<?> list = root.getChildren("prefix");
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Element ele = (Element)iterator.next();
			numEntries++;
			
			String name = ele.getChildTextNormalize("name");
			if ( name.trim().length() == 0 ) {
				continue;
			}
			
			Resource concept = _createConcept(namespace + name);
			
			concept.addProperty(nameProperty, name);
			
			String value = ele.getChildTextNormalize("value");
			concept.addProperty(valueProperty, value);
			
			List<?> symbols = ele.getChildren("symbol");
			for (Iterator<?> iterator2 = symbols.iterator(); iterator2.hasNext();) {
				Element ele2 = (Element)iterator2.next();
				Content content = ele2.getContent(0);
				String symbol = content.getValue();
				concept.addProperty(symbolProperty, symbol);
			}
		}

		props.put("entries", String.valueOf(numEntries));
		props.put("concepts", String.valueOf(numConcepts));
	}

	
	private Resource _createConcept(String uri) {
		Resource concept = model.createResource(uri, siPrefixClass);
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
