package org.mmisw.watchdog.onts.udunits.jena;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.mmisw.ont.vocabulary.Skos;
import org.mmisw.watchdog.conversion.BaseConverter;
import org.mmisw.watchdog.util.XmlUtil;

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
 * Conversion of file with &lt;unit> elements.
 * 
 * <p>
 * TODO: for name, only singular is captured
 * 
 * <p>
 * TODOL for aliases, only the singular name is captured
 * 
 * @author Carlos Rueda
 */
public class UdunitsUnitConverterJena extends BaseConverter {

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

	private Resource unitClass;
	

	private Property nameProperty;
	private Property dimensionlessProperty;
	private Property defProperty;
	private Property baseProperty;
	private Property symbolProperty;
	private Property aliasProperty;
	
	/** created concepts in SKOS ontology */
	private int numConcepts;

	
	private void _createNewOntology() {

		// create ontology
		model = Skos.createModel();
		
		model.setNsPrefix("", namespace);

		
		unitClass = model.createResource(namespace + "Unit");
		
		Statement stmt;
		
		stmt = model.createStatement(unitClass, RDF.type, OWL.Class);
		model.add(stmt);
		
		stmt = model.createStatement(unitClass, RDFS.subClassOf, Skos.Concept);
		model.add(stmt);

		stmt = model.createStatement(unitClass, RDFS.label, "Unit");
		model.add(stmt);

		Property[] props = { 
				nameProperty = model.createProperty(namespace + "name"),
				dimensionlessProperty = model.createProperty(namespace + "dimensionless"),
				defProperty = model.createProperty(namespace + "def"),
				baseProperty = model.createProperty(namespace + "base"),
				symbolProperty = model.createProperty(namespace + "symbol"),
				aliasProperty = model.createProperty(namespace + "alias"),
		};
		for ( Property prop : props ) {
			stmt = model.createStatement(prop, RDF.type, OWL.DatatypeProperty);
			model.add(stmt);
			stmt = model.createStatement(prop, RDFS.domain, unitClass);
			model.add(stmt);
			
			Resource range = prop == dimensionlessProperty ? XSD.xboolean : XSD.xstring;
			stmt = model.createStatement(prop, RDFS.range, range);
			model.add(stmt);
		}

	}

	private void _convert() throws Exception {
		SAXBuilder builder = new SAXBuilder();

		Document document = builder.build(new StringReader(inputContents));

		Element root = document.getRootElement();

		// note: there are not properities for the rot element in the prefixes XML
		//_getProperty(root, "version_number");
		
		List<?> list = root.getChildren("unit");
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Element ele = (Element) iterator.next();
			numEntries++;
			
			Element nameChild = ele.getChild("name");
			if ( nameChild == null ) {
				continue;
			}
			String name = nameChild.getChildTextNormalize("singular");
			if ( name == null || name.trim().length() == 0 ) {
				continue;
			}

			
			Resource concept = _createConcept(namespace + name);
			
			concept.addProperty(nameProperty, name);

			String comment = XmlUtil.getComment(ele);
			if ( comment != null ) {
				concept.addProperty(Skos.definition, comment.toString());
			}
			
			String dimensionless = ele.getChildTextNormalize("dimensionless");
			if ( dimensionless != null ) {
				concept.addLiteral(dimensionlessProperty, true);
			}
			
			String def = ele.getChildTextNormalize("def");
			if ( def != null && def.trim().length() > 0 ) {
				concept.addProperty(defProperty, def);
			}
			
			String base = ele.getChildTextNormalize("base");
			if ( base != null && base.trim().length() > 0 ) {
				concept.addProperty(baseProperty, base);
			}
			
			String symbol = ele.getChildTextNormalize("symbol");
			if ( symbol != null && symbol.trim().length() > 0 ) {
				concept.addProperty(symbolProperty, symbol);
			}
			
			List<?> symbols = ele.getChildren("aliases");
			for (Iterator<?> iterator3 = symbols.iterator(); iterator3.hasNext();) {
				Element ele2 = (Element)iterator3.next();
				Element aliasNameChild = ele2.getChild("name");
				if ( aliasNameChild != null ) {
					String alias = aliasNameChild.getChildTextNormalize("singular");
					if ( alias != null && alias.trim().length() > 0 ) {
						concept.addProperty(aliasProperty, alias);
					}
				}
			}
		}

		props.put("entries", String.valueOf(numEntries));
		props.put("concepts", String.valueOf(numConcepts));
	}

	
	private Resource _createConcept(String uri) {
		Resource concept = model.createResource(uri, unitClass);
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
