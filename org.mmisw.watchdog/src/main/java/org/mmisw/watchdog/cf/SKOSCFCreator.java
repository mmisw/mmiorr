package org.mmisw.watchdog.cf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mmisw.watchdog.util.SKOS;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import com.ibm.icu.text.SimpleDateFormat;


/**
 * 
 * @author bermudez
 * @author carueda
 * 
 * 
 */
public class SKOSCFCreator {

//	private boolean nextTermIsTopConcept = true;

//	private Resource conceptScheme;

	private Model model;

	private String fileIn;

	private String fileOut;

	private String NS = "http://mmisw.org/ont/cf/";

	private StringBuffer buf;

	private long begin;

	
//	private Resource skosConcept = ResourceFactory.createProperty(SKOS.NS, "Concept");
	private Resource standardNameClass = ResourceFactory.createProperty(NS, "Standard_Name");
	
	private Resource currentTopConcept;

	private Property canonical_units;

	public static void main(String[] args) throws IOException {
		String fileIn = "src/main/resources/input/cf-standard-name-table.xml";
		String fileOut = "src/main/resources/output/cf.owl";
		SKOSCFCreator creatorCF = new SKOSCFCreator(fileIn, fileOut);
		creatorCF.convertAndSave();
	}

	public SKOSCFCreator(String fileIn, String fileOut) {

		begin = System.currentTimeMillis();
		buf = new StringBuffer(1000);
		this.fileIn = fileIn;
		this.fileOut = fileOut;
		// this.NS = "http://mmisw/ont/cf/";

	}

	public void convertAndSave() throws IOException {
		createNewOntology();
		convert();
		saveNewOntology();
	}

	private void createNewOntology() {

		// created ontology
		model = SKOS.getAnSKOSModel();
		
		model.setNsPrefix("", NS);

		// creates top concept scheme
//		conceptScheme = model.createResource(NS + "cf", SKOS.ConceptScheme);
//		conceptScheme.addProperty(DC.creator, "Luis Bermudez MMI");
//		conceptScheme.addProperty(DC.date, (new java.text.SimpleDateFormat(
//				"yyyy-MM-dd'T'hh:mm:ss")).format(new Date(System
//				.currentTimeMillis())));
//		conceptScheme.addProperty(DC.description, "CF Terms");

		
		standardNameClass = model.createProperty(NS, "Standard_Name");
		
		currentTopConcept = model
				.createResource(NS + "parameter", standardNameClass);
//				.createResource(NS + "parameter", skosConcept);
//				.createResource(NS + "parameter", SKOS.Concept);

		Statement stmt = model.createStatement(standardNameClass, RDF.type, OWL.Class);
		model.add(stmt);
		
		stmt = model.createStatement(standardNameClass, RDFS.subClassOf, SKOS.Concept);
		model.add(stmt);

		stmt = model.createStatement(standardNameClass, RDFS.label, "Standard Name");
		model.add(stmt);
		
		stmt = model.createStatement(standardNameClass, RDFS.label, "Standard Name");
		model.add(stmt);

		
		canonical_units = model.createProperty(NS + "canonical_units");
		
		stmt = model.createStatement(canonical_units, RDF.type, OWL.DatatypeProperty);
		model.add(stmt);
		stmt = model.createStatement(canonical_units, RDFS.domain, standardNameClass);
		model.add(stmt);
		stmt = model.createStatement(canonical_units, RDFS.range, XSD.xstring);
		model.add(stmt);


	}

	private void convert() throws IOException {
		try {
			// a builder takes a boolean value meaning validation mode:
			SAXBuilder builder = new SAXBuilder();

			// simply load the document::
			Document document;

			document = builder.build(new File(fileIn));

			Element standard_name_table = document.getRootElement();
			List list   = standard_name_table.getChildren("entry");
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Element ele = (Element)iterator.next();
				String id = (ele.getAttribute("id").getValue()).trim();
				
				String canonicalUnits = ele.getChildTextNormalize("canonical_units");
//				String grib = ele.getChildTextNormalize("grib");
//				String amip = ele.getChildTextNormalize("amip");
				
				String description = ele.getChildTextNormalize("description");
				
				id = cleanStringforID(id);
				
				String prefLabel = id.replace('_', ' ');
				
				Resource concept = model.createResource(NS + id,
						standardNameClass);
//						skosConcept);
//						SKOS.Concept);
				
				concept.addProperty(SKOS.prefLabel, prefLabel);
				concept.addProperty(RDFS.label, id);
				concept.addProperty(canonical_units, canonicalUnits);
				concept.addProperty(RDFS.comment, description);
				
				currentTopConcept.addProperty(SKOS.narrower, concept);
				
				appendAction("Res created "+concept);
				
			}
			
			
			

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

	

	private void appendAction(String action) {
		buf.append(action + "\n");
		System.out.println(action);
	}

	
	public void outputLog(String logFile) {
		try {
			File f = new File(logFile);
			FileWriter fw = new FileWriter(f);
			fw.write(buf.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void saveNewOntology() {

		RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
		writer.setProperty("showXmlDeclaration", "true");
		writer.setProperty("relativeURIs", "same-document,relative");
		writer.setProperty("xmlbase", NS);
		try {
			FileOutputStream fo = new FileOutputStream(fileOut);
			// model.setNsPrefix("",NS);
			// model.write(fo,"RDF/XML-ABBREV");

			writer.write(model, fo, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		appendAction("Time in sec of the conversion: "
				+ (System.currentTimeMillis() - begin) / 1000);
		appendAction("New SKOS Ontology saved in: " + fileOut);
		appendAction("Size of the new Ontology: " + model.size());
		File file = new File(fileOut);
		Date date = new Date(file.lastModified());
		SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd'T'H:mm:ss");
		;

		appendAction("Time stamp of the file " + df.format(date));

	}

	private String cleanStringforID(String s) {
		return s.trim();

	}

}
