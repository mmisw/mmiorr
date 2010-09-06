package org.mmisw.orrclient.core.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;


/**
 * SKOS vocabulary elements used by the ORR modules.
 * 
 * @author  Luis Bermudez, Carlos Rueda
*/

public class Skos {
	private static Model model = ModelFactory.createDefaultModel();

	public static final String NS = "http://www.w3.org/2004/02/skos/core#";

	private static final Property property(String local) {
		return model.createProperty(NS, local);
	}
    
	public static final Resource Concept = model.createResource(NS + "Concept", RDFS.Class);
	
	public static final Property prefLabel = property("prefLabel");
	public static final Property altLabel = property("altLabel");
	public static final Property hiddenLabel = property("hiddenLabel");
	
	public static final Property definition = property("definition");
	
	public static final Property note = property("note");
	public static final Property changeNote = property("changeNote");
	public static final Property editorialNote = property("editorialNote");
	public static final Property example = property("example");
	public static final Property historyNote = property("historyNote");
	public static final Property scopeNote = property("scopeNote");
	
	
	public static final Property hasTopConcept = property("hasTopConcept");


	public static final Property broader = property("broader");
	public static final Property narrower = property("narrower");
	public static final Property related = property("related");
	
	
	public static final Property exactMatch = property("exactMatch");
	public static final Property closeMatch = property("closeMatch");
	public static final Property broadMatch = property("broadMatch");
	public static final Property narrowMatch = property("narrowMatch");
	public static final Property relatedMatch = property("relatedMatch");
	
	
	
	public static Model createModel(){
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefix("skos", NS);
		m.add(model);
		
//		m.add(Skos.broader, OWL.inverseOf, Skos.narrower);
//		m.add(Skos.broader, RDF.type,  OWL.TransitiveProperty);
//		m.add(Skos.broader, RDFS.range,  OWL.TransitiveProperty);
//		m.add(Skos.narrower, RDF.type,  OWL.TransitiveProperty);
//		m.add(Skos.related, RDF.type, OWL.SymmetricProperty);
		
		return m;
	}
	
	public static Resource addConceptSubClass(Model model, String conceptUri ) {
		Resource conceptSubClass = model.createResource(conceptUri);
		
		Statement stmt;
		
		stmt = model.createStatement(conceptSubClass, RDF.type, OWL.Class);
		model.add(stmt);
		
		stmt = model.createStatement(conceptSubClass, RDFS.subClassOf, Skos.Concept);
		model.add(stmt);

		return conceptSubClass;
	}
	
	public static Property addDatatypeProperty(Model model,
			Resource conceptSubClass,
			String propUri, String propLabel 
	) {
		Property prop = model.createProperty(propUri);
		
		Statement stmt;
		
		stmt = model.createStatement(prop, RDF.type, OWL.DatatypeProperty);
		model.add(stmt);
		stmt = model.createStatement(prop, RDFS.domain, conceptSubClass);
		model.add(stmt);
		stmt = model.createStatement(prop, RDFS.range, XSD.xstring);
		model.add(stmt);

		if ( propLabel != null ){
			stmt = model.createStatement(prop, RDFS.label, propLabel);
			model.add(stmt);
		}

		return prop;
	}
	
}
