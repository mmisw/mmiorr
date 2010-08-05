package org.mmisw.iserver.core.util;

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
 * @author  Luis Bermudez, Carlos Rueda
*/

public class Skos {
	private static Model model = ModelFactory.createDefaultModel();

	public static String NS = "http://www.w3.org/2004/02/skos/core#";

//	public static Resource ConceptScheme = model.createResource(NS
//			+ "ConceptScheme", RDFS.Class);
//
	public static Resource Concept = model.createResource(NS + "Concept",
			RDFS.Class);
	
	public static Property prefLabel = model.createProperty(NS + "prefLabel");
	public static Property altLabel = model.createProperty(NS + "altLabel");
	public static Property hiddenLabel = model.createProperty(NS + "hiddenLabel");
	
	public static Property definition = model.createProperty(NS + "definition");
	
	public static Property note = model.createProperty(NS + "note");
	public static Property changeNote = model.createProperty(NS + "changeNote");
	public static Property editorialNote = model.createProperty(NS + "editorialNote");
	public static Property example = model.createProperty(NS + "example");
	public static Property historyNote = model.createProperty(NS + "historyNote");
	public static Property scopeNote = model.createProperty(NS + "scopeNote");
	
	
	public static Property hasTopConcept = 
		model.createProperty(NS + "hasTopConcept");


	public static Property broader = 
			model.createProperty(NS + "broader");
	public static Property narrower = 
		model.createProperty(NS + "narrower");
	public static Property related = 
		model.createProperty(NS + "related");
	
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
